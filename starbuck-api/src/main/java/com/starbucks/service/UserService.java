package com.starbucks.service;

import com.starbucks.entity.UserInfo;
import com.starbucks.exception.EntityNotFoundException;
import com.starbucks.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserInfoRepository userRepo;
    private final RedisService redisService;
    private final WeChatService weChatService;

    @Transactional
    public UserInfo registerOrLogin(String code) throws Exception {
        String openid = weChatService.getOpenid(code);
        return userRepo.findByWxOpenid(openid)
                .orElseGet(() -> createNewUser(openid));
    }

    private UserInfo createNewUser(String openid) {
        UserInfo newUser = new UserInfo();
        newUser.setNickname("星巴克用户" + RandomStringUtils.randomNumeric(6));
        newUser.setMoney(0);
        newUser.setWxOpenid(openid);
        UserInfo saved = userRepo.save(newUser);
        redisService.cacheUser(saved);
        return saved;
    }

    /**
     * 用户名密码登录
     */
    public UserInfo loginByUsername(String username, String password) {
        UserInfo user = userRepo.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        
        if (!password.equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        return user;
    }

    /**
     * 用户注册
     */
    @Transactional
    public UserInfo register(UserInfo userInfo) {
        // 检查用户名是否已存在
        if (userInfo.getUsername() == null || userInfo.getUsername().trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (userRepo.findByUsername(userInfo.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查手机号是否已存在
        if (userInfo.getUserPhone() == null || userInfo.getUserPhone().trim().isEmpty()) {
            throw new RuntimeException("手机号不能为空");
        }
        if (userRepo.findByUserPhone(userInfo.getUserPhone()).isPresent()) {
            throw new RuntimeException("手机号已被注册");
        }

        // 检查密码
        if (userInfo.getPassword() == null || userInfo.getPassword().trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }

        // 设置默认值
        if (userInfo.getMoney() == null) {
            userInfo.setMoney(0);
        }

        UserInfo saved = userRepo.save(userInfo);
        redisService.cacheUser(saved);
        return saved;
    }

    @Transactional
    public UserInfo updateUser(UserInfo user) {
        UserInfo updated = userRepo.save(user);
        redisService.cacheUser(updated);
        return updated;
    }

    public UserInfo getUserInfo(Integer userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
    }

    /**
     * 检查用户名是否存在
     */
    public boolean checkUsernameExists(String username) {
        return userRepo.findByUsername(username).isPresent();
    }

    /**
     * 检查手机号是否存在
     */
    public boolean checkPhoneExists(String phone) {
        return userRepo.findByUserPhone(phone).isPresent();
    }
}