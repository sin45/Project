package com.starbucks.controller;

import com.starbucks.entity.BalanceLog;
import com.starbucks.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/balance")
@RequiredArgsConstructor
public class BalanceController {
    private final BalanceService balanceService;

    @PostMapping("/recharge/{userId}")
    public java.util.Map<String, Object> recharge(
            @PathVariable Integer userId,
            @RequestParam Integer amount,
            @RequestParam String remarks) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        try {
            balanceService.changeBalance(
                    userId,
                    amount,
                    BalanceLog.ChangeType.RECHARGE,
                    remarks
            );
            result.put("success", true);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/payment/{userId}")
    public java.util.Map<String, Object> payment(
            @PathVariable Integer userId,
            @RequestParam Integer amount,
            @RequestParam String remarks) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        try {
            balanceService.changeBalance(
                    userId,
                    -amount,
                    BalanceLog.ChangeType.PAYMENT,
                    remarks
            );
            result.put("success", true);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
}