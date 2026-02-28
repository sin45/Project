package com.starbucks.controller;

import com.alibaba.fastjson2.JSONObject;
import com.starbucks.util.WeChatMiniUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信消息推送测试接口
 */
@RestController
@RequestMapping("/api/wxChatMessage")
public class WeChatMessageController {

    @Autowired
    private WeChatMiniUtils weChatMiniUtils;

    /**
     * 测试发送订阅消息
     * 访问示例：http://localhost:8080/sendMessage?openid=o6_bmjrPTlm6_2sgVt7hMZOPfL2M&templateId=eTM7xxxxxxxxx
     */
    @GetMapping("/sendMessage")
    public String sendMessage(
            @RequestParam String openid,
            @RequestParam String templateId) {

        // 组装模板参数（需和你的订阅模板字段完全匹配）
        JSONObject data = new JSONObject();
        data.put("thing3", new JSONObject().fluentPut("value", "拿铁*3"));
        data.put("phrase4", new JSONObject().fluentPut("value", "新订单"));
        data.put("thing5", new JSONObject().fluentPut("value", "无"));
        data.put("amount6", new JSONObject().fluentPut("amount6", "50"));
        data.put("character_string2", new JSONObject().fluentPut("value", "121212"));

        // 发送消息（跳转页面：/pages/order/detail?orderId=123）
        boolean result = weChatMiniUtils.sendSubscribeMessage(
                openid,
                templateId,
                "pages/orderManage/orderManage",
                data
        );

        return result ? "消息发送成功" : "消息发送失败";
    }
}
