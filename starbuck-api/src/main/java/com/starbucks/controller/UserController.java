package com.starbucks.controller;

import com.starbucks.entity.UserInfo;
import com.starbucks.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserInfo> getUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(userService.getUserInfo(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserInfo> updateUser(
            @PathVariable Integer userId,
            @RequestBody UserInfo user) {
        user.setUserId(userId);
        return ResponseEntity.ok(userService.updateUser(user));
    }

    /**
     * 用户名密码登录
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        if (username == null || password == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户名和密码不能为空");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            UserInfo user = userService.loginByUsername(username, password);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("user", user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 微信登录
     */
    @PostMapping("/login/wechat")
    public ResponseEntity<Map<String, Object>> wechatLogin(@RequestBody Map<String, String> loginRequest) {
        String code = loginRequest.get("code");
        
        if (code == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "微信授权码不能为空");
            ResponseEntity<Map<String, Object>> responseEntity = ResponseEntity.badRequest().body(errorResponse);
            return responseEntity;
        }
        
        try {
            UserInfo user = userService.registerOrLogin(code);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("user", user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody UserInfo userInfo) {
        try {
            UserInfo registeredUser = userService.register(userInfo);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("user", registeredUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam String username) {
        boolean exists = userService.checkUsernameExists(username);
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("available", !exists);
        return ResponseEntity.ok(response);
    }

    /**
     * 检查手机号是否可用
     */
    @GetMapping("/check-phone")
    public ResponseEntity<Map<String, Object>> checkPhone(@RequestParam String phone) {
        boolean exists = userService.checkPhoneExists(phone);
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("available", !exists);
        return ResponseEntity.ok(response);
    }
}