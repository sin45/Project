package com.starbucks.controller;

import com.starbucks.entity.OrderDetail;
import com.starbucks.repository.OrderDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orderdetail")
public class OrderDetailController {
    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @GetMapping("/{orderId}")
    public List<OrderDetail> getOrderDetailsByOrderId(@PathVariable Integer orderId) {
        return orderDetailRepository.findByOrderId(orderId);
    }
} 