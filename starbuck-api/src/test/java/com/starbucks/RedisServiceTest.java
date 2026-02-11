package com.starbucks;

import com.starbucks.entity.UserInfo;
import com.starbucks.service.RedisService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RedisServiceTest {

    @Autowired
    private RedisService redisService;

    @Test
    public void testCacheAndGetUser() {
        // 构造一个用户对象
        UserInfo user = new UserInfo();
        user.setUserId(123);
        user.setUsername("redis_test");
        user.setNickname("Redis测试");
        user.setMoney(999);
        user.setUserPhone("18888888888");
        user.setUserBirth("2000-01-01");

        // 缓存到 Redis
        redisService.cacheUser(user);

        // 从 Redis 读取
        UserInfo cached = redisService.getUserById(123).orElse(null);

        // 校验
        Assertions.assertNotNull(cached);
        Assertions.assertEquals(user.getUserId(), cached.getUserId());
        Assertions.assertEquals(user.getUsername(), cached.getUsername());
        Assertions.assertEquals(user.getNickname(), cached.getNickname());
        Assertions.assertEquals(user.getMoney(), cached.getMoney());
        Assertions.assertEquals(user.getUserPhone(), cached.getUserPhone());
        Assertions.assertEquals(user.getUserBirth(), cached.getUserBirth());
    }
} 