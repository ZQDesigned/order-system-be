package com.yiyunnetwork.order.service.impl;

import com.yiyunnetwork.order.exception.BusinessException;
import com.yiyunnetwork.order.model.VerificationCode;
import com.yiyunnetwork.order.repository.VerificationCodeRepository;
import com.yiyunnetwork.order.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final StringRedisTemplate stringRedisTemplate;
    
    // 仍然保留数据库操作，但只作为二级备份
    private final VerificationCodeRepository verificationCodeRepository;

    @Qualifier("taskExecutor")
    private final TaskExecutor taskExecutor;
    
    @Value("${verification.code.expiration:600}")
    private int codeExpiration; // 验证码有效期，单位秒，默认10分钟
    
    @Value("${verification.code.cooldown:120}")
    private int codeCooldown; // 冷却时间，单位秒，默认120秒
    
    private final Random random = new Random();
    
    // Redis Key 前缀
    private static final String VERIFICATION_CODE_KEY_PREFIX = "verification:code:";
    private static final String COOLDOWN_KEY_PREFIX = "verification:cooldown:";

    /**
     * 生成6位数字验证码
     */
    private String generateRandomCode() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    @Override
    public VerificationCode generateCode(String email) {
        if (!canSendCode(email)) {
            throw new BusinessException("验证码发送过于频繁，请" + codeCooldown / 60 + "分钟后再试");
        }
        
        String code = generateRandomCode();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.plusSeconds(codeExpiration);
        
        // 存入Redis，设置过期时间
        String codeKey = VERIFICATION_CODE_KEY_PREFIX + email;
        stringRedisTemplate.opsForValue().set(codeKey, code, codeExpiration, TimeUnit.SECONDS);
        
        // 设置冷却期
        String cooldownKey = COOLDOWN_KEY_PREFIX + email;
        stringRedisTemplate.opsForValue().set(cooldownKey, "1", codeCooldown, TimeUnit.SECONDS);
        
        // 构建验证码对象
        VerificationCode verificationCode = VerificationCode.builder()
                .email(email)
                .code(code)
                .expireTime(expireTime)
                .used(false)
                .build();
        
        // 异步保存到数据库作为备份，避免阻塞响应
        final String finalCode = code;
        final LocalDateTime finalExpireTime = expireTime;
        taskExecutor.execute(() -> {
            try {
                VerificationCode codeToSave = VerificationCode.builder()
                        .email(email)
                        .code(finalCode)
                        .expireTime(finalExpireTime)
                        .used(false)
                        .build();
                verificationCodeRepository.save(codeToSave);
            } catch (Exception e) {
                log.error("Failed to save verification code to database for email: " + email, e);
            }
        });
        
        return verificationCode;
    }

    @Override
    public boolean verifyCode(String email, String code) {
        // 首先从Redis中查询
        String codeKey = VERIFICATION_CODE_KEY_PREFIX + email;
        String storedCode = stringRedisTemplate.opsForValue().get(codeKey);
        
        if (storedCode != null && storedCode.equals(code)) {
            return true;
        }
        
        // 如果Redis中没有，则从数据库查询（作为备份）
        return verificationCodeRepository
                .findByEmailAndCodeAndUsedAndExpireTimeAfter(email, code, false, LocalDateTime.now())
                .isPresent();
    }

    @Override
    public void markCodeAsUsed(String email, String code) {
        // 从Redis中删除验证码
        String codeKey = VERIFICATION_CODE_KEY_PREFIX + email;
        stringRedisTemplate.delete(codeKey);
        
        // 异步在数据库中标记为已使用
        taskExecutor.execute(() -> {
            try {
                verificationCodeRepository
                        .findByEmailAndCodeAndUsedAndExpireTimeAfter(email, code, false, LocalDateTime.now())
                        .ifPresent(verificationCode -> {
                            verificationCode.setUsed(true);
                            verificationCode.setUsedTime(LocalDateTime.now());
                            verificationCodeRepository.save(verificationCode);
                        });
            } catch (Exception e) {
                log.error("Failed to mark verification code as used in database for email: " + email, e);
            }
        });
    }

    @Override
    public boolean canSendCode(String email) {
        // 检查Redis中是否存在冷却期标记
        String cooldownKey = COOLDOWN_KEY_PREFIX + email;
        return !Boolean.TRUE.equals(stringRedisTemplate.hasKey(cooldownKey));
    }
} 