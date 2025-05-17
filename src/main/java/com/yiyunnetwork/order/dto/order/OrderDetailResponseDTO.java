package com.yiyunnetwork.order.dto.order;

import com.yiyunnetwork.order.dto.product.ProductSimpleDTO;
import com.yiyunnetwork.order.dto.user.UserSimpleDTO;
import com.yiyunnetwork.order.model.Order;
import com.yiyunnetwork.order.model.OrderField;
import com.yiyunnetwork.order.model.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 订单详情响应DTO，用于管理员查看订单详情
 * 包含比OrderResponseDTO更多的信息，但依然排除敏感字段
 */
@Data
@Builder
public class OrderDetailResponseDTO {
    
    private UUID id;
    private String orderNo;
    private ProductSimpleDTO product;
    private String customerEmail;
    private String customerPhone;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime completeTime;
    private String remark;
    private Map<String, String> fields;
    private List<OrderLogDetailDTO> logs;
    private UserSimpleDTO agent;
    private UserSimpleDTO admin;
    
    /**
     * 将Order实体转换为OrderDetailResponseDTO
     * 
     * @param order Order实体
     * @return OrderDetailResponseDTO
     */
    public static OrderDetailResponseDTO fromOrder(Order order) {
        if (order == null) {
            return null;
        }
        
        // 转换订单字段为Map
        Map<String, String> fieldMap = order.getFields().stream()
                .collect(Collectors.toMap(OrderField::getFieldKey, OrderField::getFieldValue));
        
        // 转换订单日志
        List<OrderLogDetailDTO> logList = order.getLogs().stream()
                .map(OrderLogDetailDTO::fromOrderLog)
                .collect(Collectors.toList());
        
        return OrderDetailResponseDTO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .product(ProductSimpleDTO.fromProduct(order.getProduct()))
                .customerEmail(order.getCustomerEmail())
                .customerPhone(order.getCustomerPhone())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createTime(order.getCreateTime())
                .updateTime(order.getUpdateTime())
                .completeTime(order.getCompleteTime())
                .remark(order.getRemark())
                .fields(fieldMap)
                .logs(logList)
                .agent(UserSimpleDTO.fromUser(order.getAgent()))
                .admin(UserSimpleDTO.fromUser(order.getAdmin()))
                .build();
    }
} 