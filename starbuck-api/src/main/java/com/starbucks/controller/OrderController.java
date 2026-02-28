// OrderController.java
package com.starbucks.controller;

import com.alibaba.fastjson2.JSONObject;
import com.starbucks.entity.Order;
import com.starbucks.entity.OrderDetail;
import com.starbucks.service.OrderService;
import com.starbucks.util.HprtCloudPrintUtil;
import com.starbucks.util.WeChatMiniUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.starbucks.dto.OrderCreateRequest;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Autowired
    private WeChatMiniUtils weChatMiniUtils;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderCreateRequest req) {
        Order order = new Order();
        order.setUserId(req.getUserId());
        order.setStoreId(req.getStoreId());
        order.setPickupTime(req.getPickupTime());
        List<OrderDetail> details = req.getOrderDetails();
        Order created = orderService.createOrder(order, details);

        //给管理者发送订阅消息
        try{
            weChatMiniUtils.sendSubscribeMessage("openid", "templateId", "跳转页", new JSONObject());
        }catch (Exception e){
            e.printStackTrace();
        }

        //打印小票
        try{
            HprtCloudPrintUtil.submitPrintTask("订单号：22222","数量：9");
        }catch (Exception e){
            e.printStackTrace();
        }

        return ResponseEntity.ok(created);
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<?> payOrder(@PathVariable Integer orderId) {
        orderService.payOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable Integer userId) {
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderDetails(@PathVariable Integer orderId) {
        return ResponseEntity.ok(orderService.getOrderDetails(orderId));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Integer orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // DTO 内部类或可单独新建文件
}