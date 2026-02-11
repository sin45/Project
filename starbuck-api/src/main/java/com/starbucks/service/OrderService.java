// OrderService.java
package com.starbucks.service;

import com.starbucks.entity.Order;
import com.starbucks.entity.OrderDetail;
import com.starbucks.enumeration.OrderStatus;
import com.starbucks.exception.EntityNotFoundException;
import com.starbucks.exception.InvalidOrderStatusException;
import com.starbucks.repository.OrderRepository;
import com.starbucks.repository.OrderDetailRepository;
import com.starbucks.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserInfoRepository userInfoRepository;
    private final BalanceService balanceService;
    private final RedisService redisService;

    @Transactional
    public Order createOrder(Order order, List<OrderDetail> details) {
        // 生成唯一订单号
        String orderNumber = "SB" + UUID.randomUUID().toString().replace("-", "").substring(0, 15);
        order.setOrderNumber(orderNumber);
        order.setCreateTime(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.CREATED);

        // 计算总金额
        BigDecimal totalAmount = details.stream()
                .map(OrderDetail::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);

        // 保存订单
        Order savedOrder = orderRepository.save(order);

        // 保存订单详情
        details.forEach(detail -> {
            detail.setOrderId(savedOrder.getOrderId());
            orderDetailRepository.save(detail);
        });

        savedOrder.setOrderDetails(details);
        
        // 缓存订单信息
        redisService.cacheOrder(savedOrder);
        redisService.cacheOrderDetails(savedOrder.getOrderId(), details);
        
        // 失效用户订单列表缓存
        redisService.invalidateUserOrders(order.getUserId());
        
        log.info("订单已创建并缓存: {}", savedOrder.getOrderId());
        return savedOrder;
    }

    @Transactional
    public void payOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("订单不存在"));

        if (order.getOrderStatus() != OrderStatus.CREATED) {
            throw new InvalidOrderStatusException("订单状态不正确，无法支付");
        }

        // 检查余额并扣款
        int amountInCents = order.getTotalAmount().multiply(new BigDecimal(100)).intValue();
        balanceService.deductBalance(order.getUserId(), amountInCents, "订单支付，订单号: " + order.getOrderNumber());

        // 更新订单状态
        order.setOrderStatus(OrderStatus.PAID);
        order.setPayTime(LocalDateTime.now());
        orderRepository.save(order);
        
        // 更新缓存
        redisService.cacheOrder(order);
        
        log.info("订单支付完成: {}", orderId);
    }

    public List<Order> getUserOrders(Integer userId) {
        // 1. 先查Redis缓存
        List<Integer> cachedOrderIds = redisService.getUserOrderIds(userId);
        if (!cachedOrderIds.isEmpty()) {
            log.debug("从缓存获取用户订单ID列表: {}", userId);
            List<Order> orders = new java.util.ArrayList<>();
            for (Integer orderId : cachedOrderIds) {
                Optional<Order> order = redisService.getOrderById(orderId);
                if (order.isPresent()) {
                    // 获取订单详情
                    Optional<List<OrderDetail>> details = redisService.getOrderDetails(orderId);
                    if (details.isPresent()) {
                        order.get().setOrderDetails(details.get());
                    }
                    orders.add(order.get());
                }
            }
            if (!orders.isEmpty()) {
                return orders;
            }
        }

        // 2. 缓存未命中，查询MySQL
        log.debug("缓存未命中，从数据库查询用户订单: {}", userId);
        List<Order> orders = orderRepository.findByUserIdOrderByCreateTimeDesc(userId);
        
        // 3. 缓存订单信息
        for (Order order : orders) {
            List<OrderDetail> details = orderDetailRepository.findByOrderId(order.getOrderId());
            order.setOrderDetails(details);
            
            redisService.cacheOrder(order);
            redisService.cacheOrderDetails(order.getOrderId(), details);
        }
        
        log.debug("用户订单已缓存: {}", userId);
        return orders;
    }

    public Order getOrderDetails(Integer orderId) {
        // 1. 先查Redis缓存
        Optional<Order> cachedOrder = redisService.getOrderById(orderId);
        if (cachedOrder.isPresent()) {
            log.debug("从缓存获取订单详情: {}", orderId);
            Order order = cachedOrder.get();
            
            // 获取订单详情
            Optional<List<OrderDetail>> details = redisService.getOrderDetails(orderId);
            if (details.isPresent()) {
                order.setOrderDetails(details.get());
            }
            
            return order;
        }

        // 2. 缓存未命中，查询MySQL
        log.debug("缓存未命中，从数据库查询订单详情: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("订单不存在"));

        List<OrderDetail> details = orderDetailRepository.findByOrderId(orderId);
        order.setOrderDetails(details);
        
        // 3. 缓存订单信息
        redisService.cacheOrder(order);
        redisService.cacheOrderDetails(orderId, details);
        
        log.debug("订单详情已缓存: {}", orderId);
        return order;
    }

    @Transactional
    public void cancelOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("订单不存在"));

        if (order.getOrderStatus() != OrderStatus.CREATED &&
                order.getOrderStatus() != OrderStatus.PAID) {
            throw new InvalidOrderStatusException("订单当前状态无法取消");
        }

        // 如果已支付，需要退款
        if (order.getOrderStatus() == OrderStatus.PAID) {
            int amountInCents = order.getTotalAmount().multiply(new BigDecimal(100)).intValue();
            balanceService.addBalance(order.getUserId(), amountInCents, "订单取消退款，订单号: " + order.getOrderNumber());
        }

        // 更新订单状态
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        
        // 更新缓存
        redisService.cacheOrder(order);
        
        log.info("订单已取消: {}", orderId);
    }

    public List<Order> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        for (Order order : orders) {
            List<OrderDetail> details = orderDetailRepository.findByOrderId(order.getOrderId());
            order.setOrderDetails(details);
        }
        return orders;
    }

    public List<Order> getUserOrdersByType(Integer userId, Order.OrderType orderType) {
        return orderRepository.findByUserIdAndOrderTypeOrderByCreateTimeDesc(userId, orderType);
    }
}