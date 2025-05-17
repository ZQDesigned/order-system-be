package com.yiyunnetwork.order.service;

/**
 * JWT黑名单服务
 * 用于管理已注销的JWT令牌
 */
public interface JwtBlacklistService {
    
    /**
     * 将令牌添加到黑名单中
     * 
     * @param token JWT令牌
     * @param username 用户名
     */
    void addToBlacklist(String token, String username);
    
    /**
     * 检查令牌是否在黑名单中
     * 
     * @param token JWT令牌
     * @return 如果令牌在黑名单中返回true，否则返回false
     */
    boolean isBlacklisted(String token);
} 