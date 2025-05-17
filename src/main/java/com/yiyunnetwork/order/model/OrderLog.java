package com.yiyunnetwork.order.model;

import com.yiyunnetwork.order.model.enums.OrderStatus;
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
@Table(name = "order_logs")
public class OrderLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private User operator;

    @Column(length = 100)
    private String operatorName;

    @Column(length = 100)
    private String operatorEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus toStatus;

    @Column(nullable = false)
    private LocalDateTime operateTime;

    @Column(length = 500)
    private String remark;

    @PrePersist
    protected void onCreate() {
        operateTime = LocalDateTime.now();
    }
} 