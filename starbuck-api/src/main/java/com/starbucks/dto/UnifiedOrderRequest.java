package com.starbucks.dto;

public class UnifiedOrderRequest {
    private String code;       // 小程序 wx.login 的 code
    private Integer amount;    // 支付金额（单位：分）
    private String description;// 商品描述

    // getter / setter ...
}