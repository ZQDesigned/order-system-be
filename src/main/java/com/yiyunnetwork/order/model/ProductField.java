package com.yiyunnetwork.order.model;

import com.yiyunnetwork.order.model.enums.FieldType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_fields")
public class ProductField implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String fieldKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FieldType fieldType;

    @Column(nullable = false)
    private Boolean required = false;

    @Column(length = 500)
    private String validationRule;
    
    @Column(length = 500)
    private String validationMessage;

    @Column(length = 200)
    private String placeholder;

    @Column(length = 5000)
    private String options;

    @Column(nullable = false)
    private Integer displayOrder = 0;
} 