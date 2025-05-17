package com.yiyunnetwork.order.dto.order;

import com.yiyunnetwork.order.model.OrderLog;
import com.yiyunnetwork.order.model.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简化版订单日志DTO，只包含基本信息，不包含敏感信息
 */
@Data
@Builder
public class OrderLogSimpleDTO {
    
    private Long id;
    private String operatorName;
    private OrderStatus fromStatus;
    private OrderStatus toStatus;
    private LocalDateTime operateTime;
    private String remark;
    
    /**
     * 将OrderLog实体转换为OrderLogSimpleDTO
     * 
     * @param orderLog OrderLog实体
     * @return OrderLogSimpleDTO
     */
    public static OrderLogSimpleDTO fromOrderLog(OrderLog orderLog) {
        if (orderLog == null) {
            return null;
        }
        
        return OrderLogSimpleDTO.builder()
                .id(orderLog.getId())
                .operatorName(orderLog.getOperatorName())
                .fromStatus(orderLog.getFromStatus())
                .toStatus(orderLog.getToStatus())
                .operateTime(orderLog.getOperateTime())
                .remark(orderLog.getRemark())
                .build();
    }
} 