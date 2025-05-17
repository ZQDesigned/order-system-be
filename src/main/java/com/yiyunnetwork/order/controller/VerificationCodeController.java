package com.yiyunnetwork.order.controller;

import com.yiyunnetwork.order.dto.ApiResult;
import com.yiyunnetwork.order.exception.BusinessException;
import com.yiyunnetwork.order.model.VerificationCode;
import com.yiyunnetwork.order.service.EmailService;
import com.yiyunnetwork.order.service.VerificationCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/verification-codes")
@RequiredArgsConstructor
@Validated
@Tag(name = "验证码管理", description = "提供验证码发送和验证接口")
public class VerificationCodeController {

    private final VerificationCodeService verificationCodeService;
    private final EmailService emailService;

    @Operation(summary = "发送验证码", description = "发送验证码到指定邮箱")
    @PostMapping("/send")
    public ApiResult<Void> sendVerificationCode(
            @Parameter(description = "邮箱地址", required = true)
            @RequestParam @Email String email) {
        try {
            // 检查是否可以发送验证码（防止频繁发送）
            if (!verificationCodeService.canSendCode(email)) {
                return ApiResult.failed("验证码发送过于频繁，请稍后再试");
            }
            
            // 生成验证码
            VerificationCode verificationCode = verificationCodeService.generateCode(email);
            
            // 发送验证码邮件
            emailService.sendVerificationCode(email, verificationCode.getCode());
            
            return ApiResult.success();
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "验证验证码", description = "验证指定邮箱和验证码是否匹配")
    @GetMapping("/verify")
    public ApiResult<Boolean> verifyCode(
            @Parameter(description = "邮箱地址", required = true)
            @RequestParam @Email String email,
            @Parameter(description = "验证码", required = true)
            @RequestParam String code) {
        return ApiResult.success(verificationCodeService.verifyCode(email, code));
    }
} 