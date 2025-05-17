package com.yiyunnetwork.order.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API限流配置
 * 使用令牌桶算法实现
 */
@Configuration
public class RateLimitConfig {
    
    @Value("${rate-limit.verification-code.capacity:3}")
    private int verificationCodeCapacity; // 验证码请求容量
    
    @Value("${rate-limit.verification-code.period:300}")
    private int verificationCodeRefillPeriod; // 验证码请求恢复周期，单位秒
    
    @Value("${rate-limit.order.capacity:10}")
    private int orderCapacity; // 订单创建容量
    
    @Value("${rate-limit.order.period:3600}")
    private int orderRefillPeriod; // 订单创建恢复周期，单位秒

    /**
     * 验证码请求限流桶
     */
    @Bean
    public Map<String, Bucket> verificationCodeBuckets() {
        return new ConcurrentHashMap<>();
    }
    
    /**
     * 订单创建限流桶
     */
    @Bean
    public Map<String, Bucket> orderBuckets() {
        return new ConcurrentHashMap<>();
    }
    
    /**
     * 获取验证码限流桶
     */
    public Bucket getVerificationCodeBucket(String key, Map<String, Bucket> buckets) {
        return buckets.computeIfAbsent(key, k -> createVerificationCodeBucket());
    }
    
    /**
     * 获取订单限流桶
     */
    public Bucket getOrderBucket(String key, Map<String, Bucket> buckets) {
        return buckets.computeIfAbsent(key, k -> createOrderBucket());
    }
    
    /**
     * 创建验证码限流桶
     */
    private Bucket createVerificationCodeBucket() {
        Bandwidth limit = Bandwidth.classic(verificationCodeCapacity, 
                Refill.greedy(verificationCodeCapacity, Duration.ofSeconds(verificationCodeRefillPeriod)));
        return Bucket4j.builder().addLimit(limit).build();
    }
    
    /**
     * 创建订单限流桶
     */
    private Bucket createOrderBucket() {
        Bandwidth limit = Bandwidth.classic(orderCapacity, 
                Refill.greedy(orderCapacity, Duration.ofSeconds(orderRefillPeriod)));
        return Bucket4j.builder().addLimit(limit).build();
    }
} 