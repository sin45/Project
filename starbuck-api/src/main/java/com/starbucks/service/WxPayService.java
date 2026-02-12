package com.starbucks.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starbucks.config.WxPayProperties;
import com.starbucks.dto.WxPayUnifiedOrderResult;
import okhttp3.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class WxPayService {

    private final PrivateKey privateKey;

    public WxPayService() {
        this.privateKey = loadPrivateKey(WxPayProperties.PRIVATE_KEY_PATH);
    }

    // 统一下单，返回 prepay_id
    public WxPayUnifiedOrderResult createJsapiOrder(Integer amountFen, String description, String openId) {
        try {
            String url = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi";

            Map<String, Object> body = new HashMap<>();
            body.put("appid", WxPayProperties.APPID);
            body.put("mchid", WxPayProperties.MCH_ID);
            body.put("description", description);
            body.put("notify_url", WxPayProperties.NOTIFY_URL);

            Map<String, Object> amount = new HashMap<>();
            amount.put("total", amountFen);
            body.put("amount", amount);

            Map<String, Object> payer = new HashMap<>();
            payer.put("openid", openId);
            body.put("payer", payer);

            String jsonBody = new ObjectMapper().writeValueAsString(body);

            // 构造 HTTP 请求（以 OkHttp 为例）
            OkHttpClient client = new OkHttpClient();

            Request request = buildSignedRequest(url, "POST", jsonBody);

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new RuntimeException("统一下单失败: " + response);
            }

            String respBody = response.body().string();
            JsonNode root = new ObjectMapper().readTree(respBody);
            String prepayId = root.get("prepay_id").asText();

            WxPayUnifiedOrderResult result = new WxPayUnifiedOrderResult();
            result.setPrepayId(prepayId);
            return result;

        } catch (Exception e) {
            throw new RuntimeException("统一下单异常", e);
        }
    }

    // 生成小程序端支付参数
    public Map<String, String> buildPayParams(String prepayId) {
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = UUID.randomUUID().toString().replace("-", "");
        String pkg = "prepay_id=" + prepayId;

        String message = WxPayProperties.APPID + "\n"
                + timeStamp + "\n"
                + nonceStr + "\n"
                + pkg + "\n";

        String paySign = sign(message, privateKey);

        Map<String, String> map = new HashMap<>();
        map.put("timeStamp", timeStamp);
        map.put("nonceStr", nonceStr);
        map.put("package", pkg);
        map.put("signType", "RSA");
        map.put("paySign", paySign);
        return map;
    }

    // 构造带微信支付签名头的 HTTP 请求
    private Request buildSignedRequest(String url, String method, String body) throws Exception {
        String nonceStr = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

        String canonicalUrl = "/v3/pay/transactions/jsapi";
        String message = method + "\n"
                + canonicalUrl + "\n"
                + timestamp + "\n"
                + nonceStr + "\n"
                + body + "\n";

        String signature = sign(message, privateKey);

        String auth = String.format(
                "WECHATPAY2-SHA256-RSA2048 mchid=\"%s\",nonce_str=\"%s\",timestamp=\"%s\",serial_no=\"%s\",signature=\"%s\"",
                WxPayProperties.MCH_ID,
                nonceStr,
                timestamp,
                WxPayProperties.MCH_SERIAL_NO,
                signature
        );

        RequestBody requestBody = RequestBody.create(
                body, MediaType.parse("application/json; charset=utf-8"));

        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", auth)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .method(method, requestBody)
                .build();
    }

    // RSA-SHA256 签名
    private String sign(String message, PrivateKey privateKey) {
        try {
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(privateKey);
            sign.update(message.getBytes(StandardCharsets.UTF_8));
            byte[] signature = sign.sign();
            return Base64.getEncoder().encodeToString(signature);
        } catch (Exception e) {
            throw new RuntimeException("签名错误", e);
        }
    }

    // 加载商户私钥
    private PrivateKey loadPrivateKey(String filePath) {
        try {
            String content = Files.readString(Paths.get(filePath), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] keyBytes = Base64.getDecoder().decode(content);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("加载私钥失败", e);
        }
    }
}