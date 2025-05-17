package com.yiyunnetwork.order.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yiyunnetwork.order.dto.ApiResult;
import com.yiyunnetwork.order.dto.auth.LoginResponseDTO;
import com.yiyunnetwork.order.dto.user.UserResponseDTO;
import com.yiyunnetwork.order.model.User;
import com.yiyunnetwork.order.service.JwtBlacklistService;
import com.yiyunnetwork.order.service.UserService;
import com.yiyunnetwork.order.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.Arrays;
import org.springframework.core.task.TaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;
    @Qualifier("taskExecutor")
    private final TaskExecutor taskExecutor;
    private final JwtBlacklistService jwtBlacklistService;
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    // 将passwordEncoder定义为静态方法，避免循环依赖
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 禁用 CSRF，因为我们是使用 Token 而不是 Cookie
        http.csrf(AbstractHttpConfigurer::disable);
        
        // 允许跨域
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        
        // 设置会话管理，因为我们使用 JWT，所以不需要会话
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        
        // 添加JWT过滤器，在用户名密码验证前处理
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        // 自定义未认证处理
        http.exceptionHandling(exceptionHandling -> 
            exceptionHandling.authenticationEntryPoint((request, response, authException) -> {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                
                ApiResult<?> result = ApiResult.failed(ApiResult.ResultCode.UNAUTHORIZED, "未授权：" + authException.getMessage());
                response.getWriter().write(objectMapper.writeValueAsString(result));
            })
        );
        
        // 设置授权规则
        http.authorizeHttpRequests(auth -> auth
                // 公开访问的API
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/orders/public/**", "/api/verification-codes/**").permitAll()
                .requestMatchers("/api/products/public/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**").permitAll()
                
                // 需要管理员权限的API
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .requestMatchers("/api/products/admin/**").hasRole("ADMIN")
                
                // 需要代理或管理员权限的API
                .requestMatchers("/api/orders/admin/**").hasAnyRole("AGENT", "ADMIN")
                
                // 其他请求需要认证
                .anyRequest().authenticated()
        );
        
        // 设置表单登录，用于管理员登录
        http.formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")
                .successHandler(loginSuccessHandler())
                .failureHandler(loginFailureHandler())
                .permitAll()
        );
        
        // 设置注销
        http.logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler(logoutSuccessHandler())
                .permitAll()
        );
        
        return http.build();
    }

    // 登录成功处理器
    private AuthenticationSuccessHandler loginSuccessHandler() {
        return (request, response, authentication) -> {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            
            // 获取当前登录用户
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            
            // 获取用户详情
            User user = userService.findByUsername(username);
            
            // 使用TaskExecutor异步更新用户最后登录时间，避免阻塞响应
            taskExecutor.execute(() -> {
                try {
                    User userToUpdate = userService.findByUsername(username);
                    userToUpdate.setLastLoginTime(LocalDateTime.now());
                    userService.save(userToUpdate);
                } catch (Exception e) {
                    logger.error("Failed to update last login time for user: " + username, e);
                }
            });
            
            // 生成JWT令牌
            String token = jwtUtil.generateToken(userDetails);
            
            // 构建响应对象，包含令牌和用户信息（使用DTO避免循环引用）
            LoginResponseDTO loginResponse = LoginResponseDTO.builder()
                    .token(token)
                    .user(UserResponseDTO.fromUser(user))
                    .build();
            
            ApiResult<LoginResponseDTO> result = ApiResult.success(loginResponse);
            response.getWriter().write(objectMapper.writeValueAsString(result));
        };
    }
    
    // 登录失败处理器
    private AuthenticationFailureHandler loginFailureHandler() {
        return (request, response, exception) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            
            ApiResult<?> result = ApiResult.failed("登录失败：" + exception.getMessage());
            response.getWriter().write(objectMapper.writeValueAsString(result));
        };
    }
    
    // 注销成功处理器
    private LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            
            // 从请求头中获取JWT令牌
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                
                try {
                    // 直接从JWT令牌中获取用户名，不依赖authentication对象
                    String username = jwtUtil.extractUsername(token);
                    
                    // 异步将令牌添加到黑名单
                    taskExecutor.execute(() -> {
                        try {
                            jwtBlacklistService.addToBlacklist(token, username);
                        } catch (Exception e) {
                            logger.error("Failed to blacklist JWT token on logout", e);
                        }
                    });
                } catch (Exception e) {
                    logger.error("Failed to extract username from JWT token", e);
                }
            }
            
            ApiResult<String> result = ApiResult.success("注销成功");
            response.getWriter().write(objectMapper.writeValueAsString(result));
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return builder.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 