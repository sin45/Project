package com.starbucks.dto;

import com.starbucks.entity.OrderDetail;
import java.time.LocalDateTime;
import java.util.List;

public class OrderCreateRequest {
    private Integer userId;
    private Integer storeId;
    private LocalDateTime pickupTime;
    private List<OrderDetail> orderDetails;

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getStoreId() { return storeId; }
    public void setStoreId(Integer storeId) { this.storeId = storeId; }
    public LocalDateTime getPickupTime() { return pickupTime; }
    public void setPickupTime(LocalDateTime pickupTime) { this.pickupTime = pickupTime; }
    public List<OrderDetail> getOrderDetails() { return orderDetails; }
    public void setOrderDetails(List<OrderDetail> orderDetails) { this.orderDetails = orderDetails; }
} 