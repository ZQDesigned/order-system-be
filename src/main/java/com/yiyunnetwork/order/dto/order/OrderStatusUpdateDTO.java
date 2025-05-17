package com.yiyunnetwork.order.dto.order;

import com.yiyunnetwork.order.model.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateDTO {
    
    @NotNull(message = "订单状态不能为空")
    private OrderStatus status;
    
    private String remark;
} 