package com.yiyunnetwork.order.repository;

import com.yiyunnetwork.order.model.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    /**
     * 查找特定邮箱的最新未使用的验证码
     */
    Optional<VerificationCode> findFirstByEmailAndUsedOrderByCreateTimeDesc(String email, Boolean used);
    
    /**
     * 查找特定邮箱和验证码
     */
    Optional<VerificationCode> findByEmailAndCodeAndUsedAndExpireTimeAfter(
            String email, String code, Boolean used, LocalDateTime now);
    
    /**
     * 查找特定时间段内发送给指定邮箱的验证码
     */
    @Query("SELECT v FROM VerificationCode v WHERE v.email = ?1 AND v.createTime > ?2")
    List<VerificationCode> findByEmailAndCreateTimeAfter(String email, LocalDateTime time);
} 