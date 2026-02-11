package com.starbucks.service;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class WeChatService {
    @Value("${wx.miniapp.appid}")
    private String appId;

    @Value("${wx.miniapp.secret}")
    private String secret;

    private WxMaService wxMaService;

    @PostConstruct
    public void init() {
        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
        config.setAppid(appId);
        config.setSecret(secret);

        wxMaService = new WxMaServiceImpl();
        wxMaService.setWxMaConfig(config);
    }

    public String getOpenid(String code) throws WxErrorException {
        return wxMaService.getUserService().getSessionInfo(code).getOpenid();
    }
}