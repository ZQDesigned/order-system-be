package com.yiyunnetwork.order.dto.product;

import com.yiyunnetwork.order.model.enums.FieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFieldDTO {
    
    private Long id;
    
    @NotBlank(message = "字段名称不能为空")
    @Size(max = 50, message = "字段名称长度不能超过50个字符")
    private String name;
    
    @NotBlank(message = "字段标识不能为空")
    @Size(max = 50, message = "字段标识长度不能超过50个字符")
    private String fieldKey;
    
    @NotNull(message = "字段类型不能为空")
    private FieldType fieldType;
    
    private Boolean required = false;
    
    @Size(max = 500, message = "验证规则长度不能超过500个字符")
    private String validationRule;
    
    @Size(max = 500, message = "验证消息长度不能超过500个字符")
    private String validationMessage;
    
    @Size(max = 200, message = "提示文本长度不能超过200个字符")
    private String placeholder;
    
    /**
     * 对于单选/多选类型，使用JSON数组存储选项
     * 例如：[{"label":"选项1","value":"1"},{"label":"选项2","value":"2"}]
     */
    @Size(max = 5000, message = "选项值长度不能超过5000个字符")
    private String options;
    
    private Integer displayOrder = 0;
} 