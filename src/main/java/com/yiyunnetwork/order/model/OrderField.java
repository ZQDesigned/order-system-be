package com.yiyunnetwork.order.model;

import com.yiyunnetwork.order.model.enums.FieldType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_fields")
public class OrderField implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, length = 50)
    private String fieldKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FieldType fieldType;

    @Column(length = 5000)
    private String fieldValue;
} 