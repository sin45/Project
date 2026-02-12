package com.starbucks.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付 v3 回调通知接口（支付结果通知）。
 * <p>
 * notify_url 示例配置为：
 * https://your-domain.com/api/pay/notify
 * <p>
 * 微信会以 POST JSON 的形式回调，并在 HTTP Header 中携带签名信息。
 * 本 Controller 只负责接收、简单记录并按 v3 规范返回响应；
 * 验签与资源解密可以在后续接入完成后补充到 TODO 部分。
 */
@RestController
@RequestMapping("/api/pay")
public class WxPayCallbackController {

    private static final Logger log = LoggerFactory.getLogger(WxPayCallbackController.class);

    /**
     * 微信支付结果回调（v3）
     * 文档：https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_5_5.shtml
     *
     * @param body       微信回调的原始 JSON 字符串
     * @param signature  HTTP 头 Wechatpay-Signature
     * @param timestamp  HTTP 头 Wechatpay-Timestamp
     * @param nonce      HTTP 头 Wechatpay-Nonce
     * @param serial     HTTP 头 Wechatpay-Serial（平台证书序列号）
     * @return 按微信 v3 规范返回 JSON：{"code":"SUCCESS","message":"成功"}
     */
    @PostMapping("/notify")
    public ResponseEntity<Map<String, String>> payNotify(
            @RequestBody String body,
            @RequestHeader("Wechatpay-Signature") String signature,
            @RequestHeader("Wechatpay-Timestamp") String timestamp,
            @RequestHeader("Wechatpay-Nonce") String nonce,
            @RequestHeader(value = "Wechatpay-Serial", required = false) String serial
    ) {
        // 记录原始回调内容及签名信息，方便后续排查
        log.info("WeChat Pay v3 notify received, body: {}", body);
        log.info("WeChat Pay v3 notify headers: signature={}, timestamp={}, nonce={}, serial={}",
                signature, timestamp, nonce, serial);

        // TODO 1：进行签名验证（使用微信平台证书）
        // TODO 2：解密 body 中 resource.ciphertext，得到具体支付结果数据
        // TODO 3：根据解密后的订单号 / 支付状态，更新自己业务系统中的订单状态
        // TODO 4：注意幂等处理（同一订单可能收到多次回调）

        // 按微信 v3 规范返回成功应答
        Map<String, String> resp = new HashMap<>();
        resp.put("code", "SUCCESS");
        resp.put("message", "成功");
        return ResponseEntity.ok(resp);
    }
}

