package com.starbucks.controller;

import com.starbucks.dto.UnifiedOrderRequest;
import com.starbucks.dto.WxPayUnifiedOrderResult;
import com.starbucks.service.WxPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pay")
public class PayController {

    @Autowired
    private WxPayService wxPayService;

    // 小程序端调用的统一下单接口
    @PostMapping("/unifiedOrder")
    public Map<String, Object> unifiedOrder() {

        int amount = 100;
        String description = "支付测试";
        String openId = "111";

        // 2. 创建你自己业务订单（略）

        // 3. 调用微信统一下单，返回 prepay_id
        WxPayUnifiedOrderResult prepay = wxPayService.createJsapiOrder(amount,description,openId);

        // 4. 生成小程序端需要的签名参数
        Map<String, String> payParams = wxPayService.buildPayParams(prepay.getPrepayId());

        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 0);
        resp.put("msg", "ok");
        resp.put("data", payParams);
        return resp;
    }
}