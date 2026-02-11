package com.starbucks.repository;

import com.starbucks.entity.BalanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BalanceLogRepository extends JpaRepository<BalanceLog, Integer> {
    List<BalanceLog> findByUser_userIdOrderByChangeTimeDesc(Integer userId);
}