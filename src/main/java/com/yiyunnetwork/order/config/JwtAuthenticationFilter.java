package com.yiyunnetwork.order.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yiyunnetwork.order.dto.ApiResult;
import com.yiyunnetwork.order.model.User;
import com.yiyunnetwork.order.service.JwtBlacklistService;
import com.yiyunnetwork.order.service.UserService;
import com.yiyunnetwork.order.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.yiyunnetwork.order.dto.ApiResult.ResultCode.UNAUTHORIZED;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final TaskExecutor taskExecutor;
    private final JwtBlacklistService jwtBlacklistService;
    
    // 登录时间更新的节流时间（分钟）
    private static final long LOGIN_TIME_THROTTLE_MINUTES = 30;
    // Redis键前缀
    private static final String LAST_LOGIN_UPDATE_KEY_PREFIX = "last_login_update:";
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   UserDetailsService userDetailsService,
                                   UserService userService,
                                   RedisTemplate<String, Object> redisTemplate,
                                   @Qualifier("taskExecutor") TaskExecutor taskExecutor,
                                   JwtBlacklistService jwtBlacklistService, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
        this.redisTemplate = redisTemplate;
        this.taskExecutor = taskExecutor;
        this.jwtBlacklistService = jwtBlacklistService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // 检查请求头中是否有JWT令牌
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                if (jwtBlacklistService.isBlacklisted(jwt)) {
                    logger.warn("Attempt to use blacklisted JWT token");
                    ApiResult<?> result = ApiResult.failed(UNAUTHORIZED, "令牌已失效，请重新登录");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(objectMapper.writeValueAsString(result));
                    return; // 立即返回，不继续处理请求
                }
                
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                log.error("JWT Token is invalid", e);
            }
        } else {
            log.debug("No JWT token found in request headers or malformed token");
        }

        // 如果存在token且当前SecurityContext中没有已经认证的用户，则验证token
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 如果token有效，则设置认证信息
            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                
                // 更新用户最后登录时间，使用节流控制更新频率
                // 排除Swagger文档请求和API文档请求
                if (!request.getRequestURI().contains("/swagger-ui") && 
                    !request.getRequestURI().contains("/api-docs")) {
                    updateLastLoginTimeWithThrottle(username);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
    
    /**
     * 更新用户最后登录时间，使用Redis进行节流控制
     * 在LOGIN_TIME_THROTTLE_MINUTES时间内只更新一次
     * 
     * @param username 用户名
     */
    private void updateLastLoginTimeWithThrottle(String username) {
        String redisKey = LAST_LOGIN_UPDATE_KEY_PREFIX + username;
        
        // 检查Redis中是否已存在该键（表示近期已更新过）
        Boolean keyExists = redisTemplate.hasKey(redisKey);
        
        // 如果键不存在，则进行更新
        if (keyExists == null || !keyExists) {
            try {
                // 异步更新用户最后登录时间
                updateUserLastLoginTimeAsync(username);
                
                // 在Redis中设置节流标志，LOGIN_TIME_THROTTLE_MINUTES分钟内不再更新
                redisTemplate.opsForValue().set(redisKey, true, LOGIN_TIME_THROTTLE_MINUTES, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.error("Failed to update or throttle last login time", e);
            }
        }
    }
    
    /**
     * 异步更新用户最后登录时间
     * 
     * @param username 用户名
     */
    private void updateUserLastLoginTimeAsync(String username) {
        taskExecutor.execute(() -> {
            try {
                User user = userService.findByUsername(username);
                user.setLastLoginTime(LocalDateTime.now());
                userService.save(user);
            } catch (Exception e) {
                log.error("Failed to update last login time asynchronously", e);
            }
        });
    }
} 