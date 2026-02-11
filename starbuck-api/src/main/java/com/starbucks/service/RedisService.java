package com.starbucks.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starbucks.entity.Order;
import com.starbucks.entity.OrderDetail;
import com.starbucks.entity.Product;
import com.starbucks.entity.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {
    private static final String USER_KEY_PREFIX = "user:";
    private static final String PRODUCT_KEY_PREFIX = "product:";
    private static final String PRODUCTS_ALL_KEY = "products:all";
    private static final String ORDER_KEY_PREFIX = "order:";
    private static final String USER_ORDERS_KEY_PREFIX = "user_orders:";
    private static final String ORDER_ITEMS_KEY_PREFIX = "order_items:";
    
    // 缓存过期时间（秒）
    private static final long USER_CACHE_TTL = 86400; // 24小时
    private static final long PRODUCT_CACHE_TTL = 3600; // 1小时
    private static final long ORDER_CACHE_TTL = 86400; // 24小时
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== 用户缓存 ====================
    
    public void cacheUser(UserInfo user) {
        String userKey = USER_KEY_PREFIX + user.getUserId();
        Map<String, String> userMap = new HashMap<>();
        if (user.getUsername() != null) userMap.put("username", user.getUsername());
        if (user.getPassword() != null) userMap.put("password", user.getPassword());
        if (user.getNickname() != null) userMap.put("nickname", user.getNickname());
        if (user.getMoney() != null) userMap.put("money", String.valueOf(user.getMoney()));
        if (user.getUserPhone() != null) userMap.put("user_phone", user.getUserPhone());
        if (user.getUserBirth() != null) userMap.put("user_birth", user.getUserBirth());
        if (user.getWxOpenid() != null) userMap.put("wx_openid", user.getWxOpenid());
        if (user.getAvatar() != null) userMap.put("avatar_url", user.getAvatar());
        
        redisTemplate.opsForHash().putAll(userKey, userMap);
        redisTemplate.expire(userKey, USER_CACHE_TTL, TimeUnit.SECONDS);
        log.debug("用户信息已缓存: {}", userKey);
    }

    public Optional<UserInfo> getUserById(Integer userId) {
        String key = USER_KEY_PREFIX + userId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        if (entries.isEmpty()) return Optional.empty();

        UserInfo user = new UserInfo();
        user.setUserId(userId);
        user.setUsername((String) entries.get("username"));
        user.setPassword((String) entries.get("password"));
        user.setNickname((String) entries.get("nickname"));
        user.setMoney(entries.get("money") != null ? Integer.valueOf(entries.get("money").toString()) : null);
        user.setUserPhone((String) entries.get("user_phone"));
        user.setUserBirth((String) entries.get("user_birth"));
        user.setWxOpenid((String) entries.get("wx_openid"));
        user.setAvatar((String) entries.get("avatar_url"));
        return Optional.of(user);
    }

    public void invalidateUser(Integer userId) {
        String key = USER_KEY_PREFIX + userId;
        redisTemplate.delete(key);
        log.debug("用户缓存已失效: {}", key);
    }

    // ==================== 产品缓存 ====================
    
    public void cacheProduct(Product product) {
        String productKey = PRODUCT_KEY_PREFIX + product.getProductId();
        try {
            String productJson = objectMapper.writeValueAsString(product);
            redisTemplate.opsForValue().set(productKey, productJson, PRODUCT_CACHE_TTL, TimeUnit.SECONDS);
            log.debug("产品信息已缓存: {}", productKey);
        } catch (JsonProcessingException e) {
            log.error("产品序列化失败: {}", product.getProductId(), e);
        }
    }

    public void cacheAllProducts(List<Product> products) {
        try {
            String productsJson = objectMapper.writeValueAsString(products);
            redisTemplate.opsForValue().set(PRODUCTS_ALL_KEY, productsJson, PRODUCT_CACHE_TTL, TimeUnit.SECONDS);
            log.debug("所有产品信息已缓存，数量: {}", products.size());
        } catch (JsonProcessingException e) {
            log.error("产品列表序列化失败", e);
        }
    }

    public Optional<Product> getProductById(Integer productId) {
        String key = PRODUCT_KEY_PREFIX + productId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) return Optional.empty();

        try {
            Product product = objectMapper.readValue(value.toString(), Product.class);
            return Optional.of(product);
        } catch (JsonProcessingException e) {
            log.error("产品反序列化失败: {}", productId, e);
            return Optional.empty();
        }
    }

    public Optional<List<Product>> getAllProducts() {
        Object value = redisTemplate.opsForValue().get(PRODUCTS_ALL_KEY);
        if (value == null) return Optional.empty();

        try {
            List<Product> products = objectMapper.readValue(value.toString(), 
                objectMapper.getTypeFactory().constructCollectionType(List.class, Product.class));
            return Optional.of(products);
        } catch (JsonProcessingException e) {
            log.error("产品列表反序列化失败", e);
            return Optional.empty();
        }
    }

    public void invalidateProduct(Integer productId) {
        String productKey = PRODUCT_KEY_PREFIX + productId;
        redisTemplate.delete(productKey);
        redisTemplate.delete(PRODUCTS_ALL_KEY); // 同时失效产品列表缓存
        log.debug("产品缓存已失效: {}", productKey);
    }

    public void invalidateAllProducts() {
        redisTemplate.delete(PRODUCTS_ALL_KEY);
        // 可以选择性地清理所有产品缓存
        Set<String> keys = redisTemplate.keys(PRODUCT_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        log.debug("所有产品缓存已失效");
    }

    // ==================== 订单缓存 ====================
    
    public void cacheOrder(Order order) {
        String orderKey = ORDER_KEY_PREFIX + order.getOrderId();
        try {
            String orderJson = objectMapper.writeValueAsString(order);
            redisTemplate.opsForValue().set(orderKey, orderJson, ORDER_CACHE_TTL, TimeUnit.SECONDS);
            
            // 缓存用户订单列表
            String userOrdersKey = USER_ORDERS_KEY_PREFIX + order.getUserId();
            redisTemplate.opsForZSet().add(userOrdersKey, order.getOrderId().toString(), 
                order.getCreateTime().toEpochSecond(java.time.ZoneOffset.UTC));
            redisTemplate.expire(userOrdersKey, ORDER_CACHE_TTL, TimeUnit.SECONDS);
            
            log.debug("订单信息已缓存: {}", orderKey);
        } catch (JsonProcessingException e) {
            log.error("订单序列化失败: {}", order.getOrderId(), e);
        }
    }

    public void cacheOrderDetails(Integer orderId, List<OrderDetail> details) {
        String itemsKey = ORDER_ITEMS_KEY_PREFIX + orderId;
        try {
            String detailsJson = objectMapper.writeValueAsString(details);
            redisTemplate.opsForValue().set(itemsKey, detailsJson, ORDER_CACHE_TTL, TimeUnit.SECONDS);
            log.debug("订单详情已缓存: {}", itemsKey);
        } catch (JsonProcessingException e) {
            log.error("订单详情序列化失败: {}", orderId, e);
        }
    }

    public Optional<Order> getOrderById(Integer orderId) {
        String key = ORDER_KEY_PREFIX + orderId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) return Optional.empty();

        try {
            Order order = objectMapper.readValue(value.toString(), Order.class);
            return Optional.of(order);
        } catch (JsonProcessingException e) {
            log.error("订单反序列化失败: {}", orderId, e);
            return Optional.empty();
        }
    }

    public Optional<List<OrderDetail>> getOrderDetails(Integer orderId) {
        String key = ORDER_ITEMS_KEY_PREFIX + orderId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) return Optional.empty();

        try {
            List<OrderDetail> details = objectMapper.readValue(value.toString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, OrderDetail.class));
            return Optional.of(details);
        } catch (JsonProcessingException e) {
            log.error("订单详情反序列化失败: {}", orderId, e);
            return Optional.empty();
        }
    }

    public List<Integer> getUserOrderIds(Integer userId) {
        String userOrdersKey = USER_ORDERS_KEY_PREFIX + userId;
        Set<Object> orderIds = redisTemplate.opsForZSet().reverseRange(userOrdersKey, 0, -1);
        if (orderIds == null) return new ArrayList<>();
        
        return orderIds.stream()
                .map(id -> Integer.valueOf(id.toString()))
                .toList();
    }

    public void invalidateOrder(Integer orderId) {
        String orderKey = ORDER_KEY_PREFIX + orderId;
        String itemsKey = ORDER_ITEMS_KEY_PREFIX + orderId;
        redisTemplate.delete(orderKey);
        redisTemplate.delete(itemsKey);
        log.debug("订单缓存已失效: {}", orderKey);
    }

    public void invalidateUserOrders(Integer userId) {
        String userOrdersKey = USER_ORDERS_KEY_PREFIX + userId;
        redisTemplate.delete(userOrdersKey);
        log.debug("用户订单列表缓存已失效: {}", userOrdersKey);
    }

    // ==================== 通用缓存管理 ====================
    
    public void clearAllCaches() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        log.info("所有缓存已清理");
    }

    public Long getCacheSize() {
        Set<String> keys = redisTemplate.keys("*");
        return keys != null ? (long) keys.size() : 0L;
    }

    public Map<String, Long> getCacheStats() {
        Map<String, Long> stats = new HashMap<>();
        Set<String> userKeys = redisTemplate.keys(USER_KEY_PREFIX + "*");
        Set<String> productKeys = redisTemplate.keys(PRODUCT_KEY_PREFIX + "*");
        Set<String> orderKeys = redisTemplate.keys(ORDER_KEY_PREFIX + "*");
        
        stats.put("user_cache_count", userKeys != null ? (long) userKeys.size() : 0L);
        stats.put("product_cache_count", productKeys != null ? (long) productKeys.size() : 0L);
        stats.put("order_cache_count", orderKeys != null ? (long) orderKeys.size() : 0L);
        stats.put("total_cache_count", getCacheSize());
        return stats;
    }
}