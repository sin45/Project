package com.starbucks;

import com.starbucks.entity.Order;
import com.starbucks.entity.OrderDetail;
import com.starbucks.enumeration.OrderStatus;
import com.starbucks.exception.EntityNotFoundException;
import com.starbucks.exception.InsufficientBalanceException;
import com.starbucks.exception.InvalidOrderStatusException;
import com.starbucks.repository.OrderDetailRepository;
import com.starbucks.repository.OrderRepository;
import com.starbucks.repository.UserInfoRepository;
import com.starbucks.service.BalanceService;
import com.starbucks.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private UserInfoRepository userInfoRepository;

    @Mock
    private BalanceService balanceService;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private List<OrderDetail> testDetails;

    @BeforeEach
    void setUp() {
        // 创建测试订单
        testOrder = new Order();
        testOrder.setUserId(1);
        testOrder.setStoreId(101);
        testOrder.setPickupTime(LocalDateTime.now().plusHours(1));

        // 创建测试订单详情
        OrderDetail detail1 = new OrderDetail();
        detail1.setProductId(1);
        detail1.setProductName("美式咖啡");
        detail1.setQuantity(2);
        detail1.setUnitPrice(new BigDecimal("25.00"));
        detail1.setSubtotal(new BigDecimal("50.00"));

        OrderDetail detail2 = new OrderDetail();
        detail2.setProductId(2);
        detail2.setProductName("拿铁咖啡");
        detail2.setQuantity(1);
        detail2.setUnitPrice(new BigDecimal("30.00"));
        detail2.setSubtotal(new BigDecimal("30.00"));

        testDetails = Arrays.asList(detail1, detail2);

        // 设置订单总金额
        testOrder.setTotalAmount(new BigDecimal("80.00"));
    }

    @Test
    void testBasicSetup() {
        // 基本设置测试
        assertNotNull(orderService);
        assertNotNull(orderRepository);
        assertNotNull(orderDetailRepository);
        assertNotNull(balanceService);
        assertNotNull(testOrder);
        assertNotNull(testDetails);
        assertEquals(2, testDetails.size());
    }

    @Test
    void createOrder_Success() {
        // 模拟保存行为
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setOrderId(1001);
            return savedOrder;
        });

        // 执行方法
        Order createdOrder = orderService.createOrder(testOrder, testDetails);

        // 验证结果
        assertNotNull(createdOrder);
        assertEquals(1001, createdOrder.getOrderId());
        assertNotNull(createdOrder.getOrderNumber());
        assertTrue(createdOrder.getOrderNumber().startsWith("SB"));
        assertEquals(OrderStatus.CREATED, createdOrder.getOrderStatus());
        assertNotNull(createdOrder.getCreateTime());
        assertEquals(new BigDecimal("80.00"), createdOrder.getTotalAmount());

        // 验证保存操作
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderDetailRepository, times(2)).save(any(OrderDetail.class));
    }

    @Test
    void payOrder_Success() {
        // 设置测试订单
        testOrder.setOrderId(1001);
        testOrder.setOrderNumber("SB123456789");
        testOrder.setOrderStatus(OrderStatus.CREATED);

        // 模拟查询行为
        when(orderRepository.findById(1001)).thenReturn(Optional.of(testOrder));

        // 执行方法
        orderService.payOrder(1001);

        // 验证结果
        assertEquals(OrderStatus.PAID, testOrder.getOrderStatus());
        assertNotNull(testOrder.getPayTime());

        // 验证操作
        verify(balanceService, times(1)).deductBalance(eq(1), eq(8000), anyString());
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    void payOrder_OrderNotFound() {
        // 模拟订单不存在
        when(orderRepository.findById(1001)).thenReturn(Optional.empty());

        // 验证异常
        assertThrows(EntityNotFoundException.class, () -> {
            orderService.payOrder(1001);
        });
    }

    @Test
    void payOrder_InvalidStatus() {
        // 设置测试订单为已支付状态
        testOrder.setOrderId(1001);
        testOrder.setOrderStatus(OrderStatus.PAID);

        // 模拟查询行为
        when(orderRepository.findById(1001)).thenReturn(Optional.of(testOrder));

        // 验证异常
        assertThrows(InvalidOrderStatusException.class, () -> {
            orderService.payOrder(1001);
        });
    }

    @Test
    void getUserOrders_Success() {
        // 创建测试订单列表
        Order order1 = new Order();
        order1.setOrderId(1001);
        order1.setUserId(1);

        Order order2 = new Order();
        order2.setOrderId(1002);
        order2.setUserId(1);

        List<Order> orders = Arrays.asList(order1, order2);

        // 模拟查询行为
        when(orderRepository.findByUserIdOrderByCreateTimeDesc(1)).thenReturn(orders);

        // 执行方法
        List<Order> result = orderService.getUserOrders(1);

        // 验证结果
        assertEquals(2, result.size());
        assertEquals(1001, result.get(0).getOrderId());
        assertEquals(1002, result.get(1).getOrderId());
    }

    @Test
    void getOrderDetails_Success() {
        // 设置测试订单
        testOrder.setOrderId(1001);

        // 创建测试订单详情
        OrderDetail detail = new OrderDetail();
        detail.setDetailId(1);
        detail.setOrderId(1001);

        // 模拟查询行为
        when(orderRepository.findById(1001)).thenReturn(Optional.of(testOrder));
        when(orderDetailRepository.findByOrderId(1001)).thenReturn(Collections.singletonList(detail));

        // 执行方法
        Order result = orderService.getOrderDetails(1001);

        // 验证结果
        assertNotNull(result);
        assertEquals(1001, result.getOrderId());
        assertEquals(1, result.getOrderDetails().size());
        assertEquals(1, result.getOrderDetails().get(0).getDetailId());
    }

    @Test
    void cancelOrder_CreatedStatus_Success() {
        // 设置测试订单为已创建状态
        testOrder.setOrderId(1001);
        testOrder.setOrderStatus(OrderStatus.CREATED);
        testOrder.setOrderNumber("SB123456789");

        // 模拟查询行为
        when(orderRepository.findById(1001)).thenReturn(Optional.of(testOrder));

        // 执行方法
        orderService.cancelOrder(1001);

        // 验证结果
        assertEquals(OrderStatus.CANCELLED, testOrder.getOrderStatus());

        // 验证操作 - 不应调用余额服务（未支付）
        verify(balanceService, never()).addBalance(anyInt(), anyInt(), anyString());
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    void cancelOrder_PaidStatus_Success() {
        // 设置测试订单为已支付状态
        testOrder.setOrderId(1001);
        testOrder.setOrderStatus(OrderStatus.PAID);
        testOrder.setOrderNumber("SB123456789");
        testOrder.setTotalAmount(new BigDecimal("80.00"));

        // 模拟查询行为
        when(orderRepository.findById(1001)).thenReturn(Optional.of(testOrder));

        // 执行方法
        orderService.cancelOrder(1001);

        // 验证结果
        assertEquals(OrderStatus.CANCELLED, testOrder.getOrderStatus());

        // 验证操作 - 应调用余额服务退款
        verify(balanceService, times(1)).addBalance(eq(1), eq(8000), anyString());
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    void cancelOrder_InvalidStatus() {
        // 设置测试订单为已完成状态
        testOrder.setOrderId(1001);
        testOrder.setOrderStatus(OrderStatus.COMPLETED);

        // 模拟查询行为
        when(orderRepository.findById(1001)).thenReturn(Optional.of(testOrder));

        // 验证异常
        assertThrows(InvalidOrderStatusException.class, () -> {
            orderService.cancelOrder(1001);
        });
    }

    @Test
    void cancelOrder_OrderNotFound() {
        // 模拟订单不存在
        when(orderRepository.findById(1001)).thenReturn(Optional.empty());

        // 验证异常
        assertThrows(EntityNotFoundException.class, () -> {
            orderService.cancelOrder(1001);
        });
    }

    @Test
    void cancelOrder_InsufficientBalance_RefundFails() {
        // 设置测试订单为已支付状态
        testOrder.setOrderId(1001);
        testOrder.setOrderStatus(OrderStatus.PAID);
        testOrder.setOrderNumber("SB123456789");
        testOrder.setTotalAmount(new BigDecimal("80.00"));

        // 模拟查询行为
        when(orderRepository.findById(1001)).thenReturn(Optional.of(testOrder));

        // 模拟退款时抛出异常
        doThrow(new RuntimeException("退款失败")).when(balanceService)
                .addBalance(anyInt(), anyInt(), anyString());

        // 执行方法并验证异常
        assertThrows(RuntimeException.class, () -> {
            orderService.cancelOrder(1001);
        });

        // 验证订单状态未改变
        assertNotEquals(OrderStatus.CANCELLED, testOrder.getOrderStatus());
    }
}