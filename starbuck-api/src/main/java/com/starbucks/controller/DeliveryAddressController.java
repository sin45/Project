package com.starbucks.controller;

import com.alibaba.fastjson2.JSONObject;
import com.starbucks.entity.DeliveryAddress;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 配送地址管理
 */
@RestController
@RequestMapping("/api/deliveryAddress")
public class DeliveryAddressController {

    @Autowired
    private EntityManager entityManager;

    /**
     * 通过openid查询配送地址
     * @param params
     * @return
     */
    @PostMapping("/getDeliveryAddress")
    public Object getDeliveryAddress(@RequestBody JSONObject params) {

        String openId = params.get("openId").toString();

        String sql = "SELECT * FROM delivery_address WHERE openid = :openid";
        Session session = entityManager.unwrap(Session.class);
        NativeQuery<DeliveryAddress> query = session.createNativeQuery(sql, DeliveryAddress.class);
        query.setParameter("openid", openId);
        List<DeliveryAddress> resultList = query.list();

        return resultList;
    }

    /**
     * 通过openid查询默认配送地址
     * @param params
     * @return
     */
    @PostMapping("/getDefaultAddress")
    public Object getDefaultAddress(@RequestBody JSONObject params) {

        String openId = params.getString("openId");

        String sql = "SELECT * FROM delivery_address " +
                "WHERE openid = :openid " +
                "ORDER BY CASE WHEN default_flag = '1' THEN 0 ELSE 1 END, id DESC";

        Session session = entityManager.unwrap(Session.class);
        NativeQuery<DeliveryAddress> query = session.createNativeQuery(sql, DeliveryAddress.class);
        query.setParameter("openid", openId);
        query.setMaxResults(1);

        List<DeliveryAddress> resultList = query.list();
        if (resultList != null && !resultList.isEmpty()) {
            return resultList.get(0);
        }

        return null;
    }

    /**
     * 保存配送地址
     * @param params
     * @return
     */
    @PostMapping("/updateAddress")
    @Transactional
    public Object updateAddress(@RequestBody JSONObject params) {

        Integer id = params.getInteger("id");
        String openId = params.getString("openId");
        String address1 = params.getString("address1");
        String address2 = params.getString("address2");
        String receiver = params.getString("receiver");
        String phone = params.getString("phone");
        String defaultFlag = params.getString("default_flag");

        if (defaultFlag == null) {
            defaultFlag = "";
        }

        DeliveryAddress address = new DeliveryAddress();
        address.setOpenid(openId);
        address.setAddress1(address1);
        address.setAddress2(address2);
        address.setReceiver(receiver);
        address.setPhone(phone);
        address.setDefault_flag(defaultFlag);

        if (id != null) {
            address.setId(id);
        }

        // 使用 JPA merge：id 为空时插入新记录，id 不为空时更新该记录
        return entityManager.merge(address);

    }

}
