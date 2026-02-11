package com.starbucks.service;

import com.starbucks.entity.Product;
import com.starbucks.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final RedisService redisService;

    /**
     * 获取所有可用产品（Cache-Aside模式）
     */
    public List<Product> getAllAvailableProducts() {
        // 1. 先查Redis缓存
        Optional<List<Product>> cachedProducts = redisService.getAllProducts();
        if (cachedProducts.isPresent()) {
            log.debug("从缓存获取产品列表，数量: {}", cachedProducts.get().size());
            return cachedProducts.get();
        }

        // 2. 缓存未命中，查询MySQL
        log.debug("缓存未命中，从数据库查询产品列表");
        List<Product> products = productRepository.findByIsAvailable(true);
        
        // 3. 写入Redis缓存
        if (!products.isEmpty()) {
            redisService.cacheAllProducts(products);
            log.debug("产品列表已缓存到Redis，数量: {}", products.size());
        }

        return products;
    }

    /**
     * 根据ID获取产品（Cache-Aside模式）
     */
    public Optional<Product> getProductById(Integer productId) {
        // 1. 先查Redis缓存
        Optional<Product> cachedProduct = redisService.getProductById(productId);
        if (cachedProduct.isPresent()) {
            log.debug("从缓存获取产品: {}", productId);
            return cachedProduct;
        }

        // 2. 缓存未命中，查询MySQL
        log.debug("缓存未命中，从数据库查询产品: {}", productId);
        Optional<Product> product = productRepository.findById(productId);
        
        // 3. 如果产品存在且可用，写入Redis缓存
        if (product.isPresent() && Boolean.TRUE.equals(product.get().getIsAvailable())) {
            redisService.cacheProduct(product.get());
            log.debug("产品已缓存到Redis: {}", productId);
        }

        return product;
    }

    /**
     * 创建新产品
     */
    @Transactional
    public Product createProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        
        // 写入缓存
        redisService.cacheProduct(savedProduct);
        
        // 失效产品列表缓存，因为列表已变更
        redisService.invalidateAllProducts();
        
        log.info("新产品已创建并缓存: {}", savedProduct.getProductId());
        return savedProduct;
    }

    /**
     * 更新产品信息
     */
    @Transactional
    public Product updateProduct(Product product) {
        Product updatedProduct = productRepository.save(product);
        
        // 更新缓存
        redisService.cacheProduct(updatedProduct);
        
        // 失效产品列表缓存，因为列表可能已变更
        redisService.invalidateAllProducts();
        
        log.info("产品信息已更新并缓存: {}", updatedProduct.getProductId());
        return updatedProduct;
    }

    /**
     * 删除产品
     */
    @Transactional
    public void deleteProduct(Integer productId) {
        productRepository.deleteById(productId);
        
        // 失效相关缓存
        redisService.invalidateProduct(productId);
        
        log.info("产品已删除，缓存已失效: {}", productId);
    }

    /**
     * 更新产品可用状态
     */
    @Transactional
    public void updateProductAvailability(Integer productId, boolean isAvailable) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setIsAvailable(isAvailable);
            productRepository.save(product);
            
            // 更新缓存
            redisService.cacheProduct(product);
            
            // 失效产品列表缓存
            redisService.invalidateAllProducts();
            
            log.info("产品可用状态已更新: {} -> {}", productId, isAvailable);
        }
    }

    /**
     * 批量更新产品价格
     */
    @Transactional
    public void batchUpdatePrices(List<Product> products) {
        for (Product product : products) {
            productRepository.save(product);
            redisService.cacheProduct(product);
        }
        
        // 失效产品列表缓存
        redisService.invalidateAllProducts();
        
        log.info("批量更新产品价格完成，数量: {}", products.size());
    }

    /**
     * 预热产品缓存
     */
    public void warmUpProductCache() {
        log.info("开始预热产品缓存...");
        List<Product> products = productRepository.findByIsAvailable(true);
        redisService.cacheAllProducts(products);
        log.info("产品缓存预热完成，数量: {}", products.size());
    }

    /**
     * 清理产品缓存
     */
    public void clearProductCache() {
        redisService.invalidateAllProducts();
        log.info("产品缓存已清理");
    }
} 