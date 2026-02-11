package com.starbucks.controller;

import com.starbucks.entity.UserInfo;
import com.starbucks.service.UserService;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> wxLogin(@RequestParam String code) {
        try {
            UserInfo user = userService.registerOrLogin(code);
            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getUserId());
            response.put("nickname", user.getNickname());
            response.put("money", user.getMoney());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("登录失败: " + e.getMessage());
        }
    }
}