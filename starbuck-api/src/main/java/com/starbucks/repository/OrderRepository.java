package com.starbucks.repository;

import com.starbucks.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserIdOrderByCreateTimeDesc(Integer userId);
    List<Order> findByUserIdAndOrderTypeOrderByCreateTimeDesc(Integer userId, Order.OrderType orderType);
}