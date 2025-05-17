package com.yiyunnetwork.order.service;

import com.yiyunnetwork.order.model.VerificationCode;

/**
 * 验证码服务接口
 */
public interface VerificationCodeService {
    
    /**
     * 为指定邮箱生成验证码
     * 
     * @param email 邮箱地址
     * @return 生成的验证码对象
     */
    VerificationCode generateCode(String email);
    
    /**
     * 验证指定邮箱和验证码是否匹配且有效
     * 
     * @param email 邮箱地址
     * @param code  验证码
     * @return 如果验证成功返回true，否则返回false
     */
    boolean verifyCode(String email, String code);
    
    /**
     * 使用成功后将验证码标记为已使用
     * 
     * @param email 邮箱地址
     * @param code  验证码
     */
    void markCodeAsUsed(String email, String code);
    
    /**
     * 检查是否可以发送新验证码
     * 防止频繁发送
     * 
     * @param email 邮箱地址
     * @return 如果可以发送返回true，否则返回false
     */
    boolean canSendCode(String email);
} 