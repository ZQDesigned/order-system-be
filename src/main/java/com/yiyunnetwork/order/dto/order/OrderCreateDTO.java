package com.yiyunnetwork.order.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDTO {
    
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    
    @NotBlank(message = "客户邮箱不能为空")
    @Email(message = "客户邮箱格式不正确")
    private String customerEmail;
    
    private String customerPhone;
    
    /**
     * 订单字段值，以key-value形式存储
     * key为字段标识，value为字段值
     */
    @Valid
    @Builder.Default
    private Map<String, String> fields = new HashMap<>();
} 