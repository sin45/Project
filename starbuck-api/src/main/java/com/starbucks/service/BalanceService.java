package com.starbucks.service;

import com.starbucks.entity.BalanceLog;
import com.starbucks.entity.UserInfo;
import com.starbucks.exception.EntityNotFoundException;
import com.starbucks.exception.InsufficientBalanceException;
import com.starbucks.repository.BalanceLogRepository;
import com.starbucks.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BalanceService {
    private final UserInfoRepository userRepo;
    private final BalanceLogRepository logRepo;
    private final RedisService redisService;

    @Transactional
    public void changeBalance(Integer userId, int amount,
                              BalanceLog.ChangeType type, String remarks) {

        UserInfo user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));

        int newBalance = user.getMoney() + amount;
        if (newBalance < 0) {
            throw new InsufficientBalanceException("余额不足");
        }

        user.setMoney(newBalance);
        userRepo.save(user);

        BalanceLog log = new BalanceLog();
        log.setUser(user);
        log.setChangeAmount(amount);
        log.setChangeType(type);
        log.setRemarks(remarks);
        logRepo.save(log);

        redisService.cacheUser(user);
    }

    @Transactional
    public void deductBalance(Integer userId, int amount, String remarks) {
        UserInfo user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        if (user.getMoney() < amount) {
            throw new InsufficientBalanceException("余额不足");
        }
        user.setMoney(user.getMoney() - amount);
        userRepo.save(user);
        BalanceLog log = new BalanceLog();
        log.setUser(user);
        log.setChangeAmount(-amount);
        log.setChangeType(BalanceLog.ChangeType.PAYMENT); // 如有 BalanceChangeType 枚举可替换
        log.setRemarks(remarks);
        logRepo.save(log);
        redisService.cacheUser(user);
    }

    @Transactional
    public void addBalance(Integer userId, int amount, String remarks) {
        UserInfo user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        user.setMoney(user.getMoney() + amount);
        userRepo.save(user);
        BalanceLog log = new BalanceLog();
        log.setUser(user);
        log.setChangeAmount(amount);
        log.setChangeType(BalanceLog.ChangeType.REFUND); // 如有 BalanceChangeType 枚举可替换
        log.setRemarks(remarks);
        logRepo.save(log);
        redisService.cacheUser(user);
    }
}