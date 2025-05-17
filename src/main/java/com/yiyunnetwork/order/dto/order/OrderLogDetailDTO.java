package com.yiyunnetwork.order.dto.order;

import com.yiyunnetwork.order.dto.user.UserSimpleDTO;
import com.yiyunnetwork.order.model.OrderLog;
import com.yiyunnetwork.order.model.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单日志详情DTO，用于管理员查看订单日志详情
 */
@Data
@Builder
public class OrderLogDetailDTO {
    
    private Long id;
    private String operatorName;
    private String operatorEmail;
    private OrderStatus fromStatus;
    private OrderStatus toStatus;
    private LocalDateTime operateTime;
    private String remark;
    private UserSimpleDTO operator;
    
    /**
     * 将OrderLog实体转换为OrderLogDetailDTO
     * 
     * @param orderLog OrderLog实体
     * @return OrderLogDetailDTO
     */
    public static OrderLogDetailDTO fromOrderLog(OrderLog orderLog) {
        if (orderLog == null) {
            return null;
        }
        
        return OrderLogDetailDTO.builder()
                .id(orderLog.getId())
                .operatorName(orderLog.getOperatorName())
                .operatorEmail(orderLog.getOperatorEmail())
                .fromStatus(orderLog.getFromStatus())
                .toStatus(orderLog.getToStatus())
                .operateTime(orderLog.getOperateTime())
                .remark(orderLog.getRemark())
                .operator(UserSimpleDTO.fromUser(orderLog.getOperator()))
                .build();
    }
} 