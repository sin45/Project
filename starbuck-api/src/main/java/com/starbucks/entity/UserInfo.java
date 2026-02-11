package com.starbucks.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_info")
public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "User_id")
    private Integer userId; // 用户的编号，从1开始升序排序，每个用户都有唯一的ID

    @Column(name = "username", length = 50)
    private String username; // 用户账号名，在登录系统和找回密码时需要

    @Column(name = "password", length = 50)
    private String password; // 用户账号唯一对应的登录密码

    @Column(name = "nickname", length = 50)
    private String nickname; // 用户的网络昵称，可随时在个人中心修改

    @Column(name = "money")
    private Integer money; // 用户星礼卡总余额，可使用余额支付

    @Column(name = "User_phone", length = 20)
    private String userPhone; // 用户的联系电话，可用于活动报名联系以及领养宠物联系

    @Column(name = "User_birth", length = 255)
    private String userBirth; // 用户生日

    @Column(name = "wx_openid", unique = true)
    private String wxOpenid;

    @Column(name = "avatar_url", length = 255)
    private String avatar;

    // getter 和 setter
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public Integer getMoney() { return money; }
    public void setMoney(Integer money) { this.money = money; }
    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    public String getUserBirth() { return userBirth; }
    public void setUserBirth(String userBirth) { this.userBirth = userBirth; }
    public String getWxOpenid() { return wxOpenid; }
    public void setWxOpenid(String wxOpenid) { this.wxOpenid = wxOpenid; }
    public Integer getId() { return userId; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}