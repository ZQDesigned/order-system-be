package com.yiyunnetwork.order.controller;

import com.yiyunnetwork.order.dto.ApiResult;
import com.yiyunnetwork.order.dto.user.UserResponseDTO;
import com.yiyunnetwork.order.exception.BusinessException;
import com.yiyunnetwork.order.model.User;
import com.yiyunnetwork.order.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "提供登录和当前用户信息接口")
public class AuthController {

    private final UserService userService;

    // 注册功能已移除，只允许管理员创建用户
    
    @Operation(
        summary = "用户登录", 
        description = "使用用户名和密码登录系统。\n\n" +
                "**重要调用说明**：\n\n" +
                "1. 发送 **POST** 请求到 `/api/auth/login`\n" +
                "2. Content-Type: **application/x-www-form-urlencoded**\n" +
                "3. 表单参数：\n" +
                "   - username: 用户名\n" +
                "   - password: 密码\n\n" +
                "4. 响应：\n" +
                "   - 登录成功: 返回状态码 200 和 JSON 格式成功信息，包含JWT令牌和用户信息\n" +
                "   - 登录失败: 返回状态码 401 和错误信息\n\n" +
                "注意：该接口不能通过Swagger UI直接测试，请使用Postman或其他HTTP客户端工具。"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "登录表单数据", 
            required = true, 
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/x-www-form-urlencoded",
                    schema = @Schema(implementation = LoginForm.class))
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "登录成功",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginSuccessResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401", 
                    description = "登录失败，用户名或密码错误",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginFailureResponse.class))
            )
    })
    @PostMapping("/login")
    public void login() {
        // 不需要实现，由Spring Security处理
        // 此方法仅用于生成API文档
    }
    
    // 内部类，仅用于API文档生成
    private static class LoginForm {
        @Schema(description = "用户名", required = true, example = "admin")
        private String username;
        
        @Schema(description = "密码", required = true, example = "password")
        private String password;
    }
    
    // 登录成功响应文档
    private static class LoginSuccessResponse {
        @Schema(description = "状态码", example = "200")
        private Integer code;
        
        @Schema(description = "提示信息", example = "操作成功")
        private String message;
        
        @Schema(description = "数据", implementation = LoginResponseData.class)
        private Object data;
    }
    
    private static class LoginResponseData {
        @Schema(description = "JWT令牌", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTYxNTIyMzYwMCwiaWF0IjoxNjE1MTM3MjAwfQ.3DQn...")
        private String token;
        
        @Schema(description = "用户信息")
        private Object user;
    }
    
    // 登录失败响应文档
    private static class LoginFailureResponse {
        @Schema(description = "状态码", example = "401")
        private Integer code;
        
        @Schema(description = "错误信息", example = "登录失败：用户名或密码错误")
        private String message;
        
        @Schema(description = "数据", example = "null")
        private Object data;
    }

    @Operation(
            summary = "用户注销",
            description = "退出登录系统。\n\n" +
                    "**重要调用说明**：\n\n" +
                    "1. 发送 **POST** 请求到 `/api/auth/logout`\n" +
                    "2. 无需参数\n\n" +
                    "3. 响应：\n" +
                    "   - 注销成功: 返回状态码 200 和 JSON 格式成功信息\n\n" +
                    "注意：该接口不能通过Swagger UI直接测试，请使用Postman或其他HTTP客户端工具。"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "注销成功",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginSuccessResponse.class))
            )
    })
    @PostMapping("/logout")
    public void logout() {
        // 不需要实现，由Spring Security处理
        // 此方法仅用于生成API文档
    }

    @Operation(summary = "获取当前登录用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/current-user")
    public ApiResult<UserResponseDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                User user = userService.findByUsername(authentication.getName());
                return ApiResult.success(UserResponseDTO.fromUser(user));
            } catch (BusinessException e) {
                return ApiResult.failed(e.getMessage());
            }
        }
        return ApiResult.failed(ApiResult.ResultCode.UNAUTHORIZED, "未登录或登录已过期");
    }
} 