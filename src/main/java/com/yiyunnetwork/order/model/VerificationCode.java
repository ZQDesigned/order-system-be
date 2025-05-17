package com.yiyunnetwork.order.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "verification_codes")
public class VerificationCode implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expireTime;

    @Column(nullable = false)
    private LocalDateTime createTime;

    @Column
    private LocalDateTime usedTime;

    @Column(nullable = false)
    @Builder.Default
    private Boolean used = false;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
} 