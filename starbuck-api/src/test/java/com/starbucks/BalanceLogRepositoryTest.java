package com.starbucks;

import com.starbucks.entity.BalanceLog;
import com.starbucks.entity.UserInfo;
import com.starbucks.repository.BalanceLogRepository;
import com.starbucks.repository.UserInfoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@SpringBootTest
public class BalanceLogRepositoryTest {

    @Autowired
    private BalanceLogRepository balanceLogRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Test
    @Transactional
    public void testSaveBalanceLog() {
        // 先插入一个用户
        UserInfo user = new UserInfo();
        user.setUsername("balance_user");
        user.setPassword("pass");
        user.setNickname("余额测试");
        user.setMoney(500);
        user.setUserPhone("19999999999");
        user.setUserBirth("1999-09-09");
        user = userInfoRepository.save(user);

        // 插入余额日志
        BalanceLog log = new BalanceLog();
        log.setUser(user);
        log.setChangeAmount(200);
        log.setChangeType(BalanceLog.ChangeType.RECHARGE);
        log.setChangeTime(LocalDateTime.now());
        log.setRemarks("测试充值");

        BalanceLog saved = balanceLogRepository.save(log);

        Assertions.assertNotNull(saved.getLogId());
        Assertions.assertEquals(200, saved.getChangeAmount());
    }
}