package com.yiyunnetwork.order.dto.product;

import com.yiyunnetwork.order.model.Product;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 简化版产品DTO，只包含基本信息
 */
@Data
@Builder
public class ProductSimpleDTO {
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    
    /**
     * 将Product实体转换为ProductSimpleDTO
     * 
     * @param product Product实体
     * @return ProductSimpleDTO
     */
    public static ProductSimpleDTO fromProduct(Product product) {
        if (product == null) {
            return null;
        }
        
        return ProductSimpleDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .build();
    }
} 