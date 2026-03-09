package com.starbucks.controller;

import com.alibaba.fastjson2.JSONObject;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 获取管理人员的openid - 前端判断显示所有订单
 */
@RestController
@RequestMapping("/api/wxManage")
public class WxManageController {

    @Autowired
    private EntityManager entityManager;

    @PostMapping("/getManageOpenId")
    public Object getManageOpenId(@RequestBody JSONObject params) {

        String openId = params.get("openId").toString();

        String sql = "SELECT id FROM system_user where openid=:openid";
        Session session = entityManager.unwrap(Session.class);
        NativeQuery<Object[]> query = session.createNativeQuery(sql);
        query.setParameter("openid", openId);
        List<Object[]> resultList = query.list();
        if (resultList != null && resultList.size() > 0) {
            return true;
        }

        return false;
    }

    public static void main(String[] args) {
        System.out.println("oujyW63seCLX1uJCa_6oK_ZQ-DN8".length());
    }


}
