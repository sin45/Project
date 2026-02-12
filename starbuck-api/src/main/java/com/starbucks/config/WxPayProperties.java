package com.starbucks.config;

public class WxPayProperties {
    // 小程序 appid
    public static final String APPID = "wx1234567890abcdef";
    // 商户号 mchid
    public static final String MCH_ID = "1234567890";
    // 商户 API v3 密钥
    public static final String API_V3_KEY = "你的APIv3密钥";
    // 商户证书序列号
    public static final String MCH_SERIAL_NO = "你的证书序列号";
    // 商户私钥路径（.pem）
    public static final String PRIVATE_KEY_PATH = "/path/to/apiclient_key.pem";
    // 支付结果回调地址
    public static final String NOTIFY_URL = "https://your-domain.com/api/pay/notify";
}