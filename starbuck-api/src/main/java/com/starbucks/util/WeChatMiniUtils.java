package com.starbucks.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 微信小程序消息推送工具类
 */
@Component
@Slf4j
public class WeChatMiniUtils {

    // 从配置文件读取小程序参数
    @Value("${wx.miniapp.appid}")
    private String appId;

    @Value("${wx.miniapp.secret}")
    private String appSecret;

    @Value("${wx.miniapp.access-token-url}")
    private String accessTokenUrl;

    @Value("${wx.miniapp.send-message-url}")
    private String sendMessageUrl;

    // Redis模板，用于缓存access_token
    @Autowired
    private StringRedisTemplate redisTemplate;

    // access_token在Redis中的key
    private static final String ACCESS_TOKEN_KEY = "wechat_mini_access_token";
    // access_token有效期（7100秒，比微信的7200秒少100秒，避免过期）
    private static final long ACCESS_TOKEN_EXPIRE = 7100L;

    /**
     * 获取access_token（优先从Redis缓存获取，缓存失效则重新请求）
     */
    public String getAccessToken() {
        // 1. 先从Redis获取缓存的access_token
        String accessToken = redisTemplate.opsForValue().get(ACCESS_TOKEN_KEY);
        if (accessToken != null && !accessToken.isEmpty()) {
            return accessToken;
        }

        // 2. 缓存失效，调用微信接口获取新的access_token
        String url = String.format("%s?grant_type=client_credential&appid=%s&secret=%s",
                accessTokenUrl, appId, appSecret);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String result = EntityUtils.toString(entity);
                    JSONObject jsonObject = JSON.parseObject(result);
                    // 检查是否获取成功
                    if (jsonObject.containsKey("errcode") && jsonObject.getIntValue("errcode") != 0) {
                        throw new RuntimeException("获取access_token失败：" + jsonObject.getString("errmsg"));
                    }
                    // 获取新的access_token并缓存到Redis
                    accessToken = jsonObject.getString("access_token");
                    redisTemplate.opsForValue().set(ACCESS_TOKEN_KEY, accessToken, ACCESS_TOKEN_EXPIRE, TimeUnit.SECONDS);
                    return accessToken;
                }
            } catch (ParseException | IOException e) {
                throw new RuntimeException("请求微信接口失败", e);
            }
        } catch (IOException e) {
            throw new RuntimeException("创建HttpClient失败", e);
        }
        return null;
    }

    /**
     * 发送订阅消息
     * @param openid 接收消息用户的openid
     * @param templateId 订阅消息模板ID
     * @param page 点击消息跳转的小程序页面（如/pages/order/detail?orderId=123）
     * @param data 模板参数（需匹配模板字段，格式：{"thing1":{"value":"内容"},"time2":{"value":"2026-02-13"}}）
     * @return 发送结果（true成功/false失败）
     */
    public boolean sendSubscribeMessage(String openid, String templateId, String page, JSONObject data) {
        // 1. 获取access_token
        String accessToken = getAccessToken();
        if (accessToken == null) {
            System.err.println("access_token获取失败，无法发送消息");
            return false;
        }

        // 2. 组装请求参数
        JSONObject requestParam = new JSONObject();
        requestParam.put("touser", openid); // 目标用户openid
        requestParam.put("template_id", templateId); // 模板ID
        requestParam.put("page", page); // 跳转页面
        requestParam.put("data", data); // 模板参数
        requestParam.put("miniprogram_state", "formal"); // 正式版：formal，测试版：trial
        requestParam.put("lang", "zh_CN"); // 语言

        // 3. 调用微信发送接口
        String url = String.format("%s?access_token=%s", sendMessageUrl, accessToken);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            // 设置请求体为JSON格式
            StringEntity entity = new StringEntity(requestParam.toJSONString(), ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    String result = EntityUtils.toString(responseEntity);
                    JSONObject resultJson = JSON.parseObject(result);
                    // 检查发送结果（errcode=0表示成功）
                    if (resultJson.getIntValue("errcode") == 0) {
                        System.out.println("消息发送成功：" + result);
                        return true;
                    } else {
                        System.err.println("消息发送失败：" + resultJson.getString("errmsg"));
                        return false;
                    }
                }
            } catch (ParseException | IOException e) {
                log.error("发送消息请求失败: {}", e);
            }
        } catch (IOException e) {
            log.error("创建HttpClient失败: {}", e);
        }
        return false;
    }
}