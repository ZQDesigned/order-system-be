package com.yiyunnetwork.order.service.impl;

import com.yiyunnetwork.order.service.JwtBlacklistService;
import com.yiyunnetwork.order.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtBlacklistServiceImpl implements JwtBlacklistService {

    private final StringRedisTemplate stringRedisTemplate;
    private final JwtUtil jwtUtil;
    
    // Redis Key 前缀
    private static final String JWT_BLACKLIST_PREFIX = "jwt:blacklist:";
    
    @Override
    public void addToBlacklist(String token, String username) {
        try {
            // 从令牌中提取过期时间
            Date expirationDate = jwtUtil.extractExpiration(token);
            
            // 如果令牌已经过期，无需添加到黑名单
            if (expirationDate.before(new Date())) {
                return;
            }
            
            // 计算剩余有效时间（毫秒）
            long ttl = expirationDate.getTime() - System.currentTimeMillis();
            
            // 如果剩余时间小于0，令牌已过期，无需添加到黑名单
            if (ttl <= 0) {
                return;
            }
            
            // 将令牌添加到Redis黑名单中，过期时间为令牌的剩余有效期
            String key = JWT_BLACKLIST_PREFIX + token;
            stringRedisTemplate.opsForValue().set(key, username, ttl, TimeUnit.MILLISECONDS);
            log.info("JWT token for user {} has been blacklisted", username);
        } catch (Exception e) {
            log.error("Error adding JWT token to blacklist", e);
        }
    }
    
    @Override
    public boolean isBlacklisted(String token) {
        String key = JWT_BLACKLIST_PREFIX + token;
        return stringRedisTemplate.hasKey(key);
    }
} 