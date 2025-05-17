package com.yiyunnetwork.order.dto.order;

import com.yiyunnetwork.order.dto.product.ProductSimpleDTO;
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
 * 订单响应DTO，用于返回给前端的订单数据
 * 只包含必要的字段，不包含敏感信息
 */
@Data
@Builder
public class OrderResponseDTO {
    
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
    private List<OrderLogSimpleDTO> logs;
    
    /**
     * 将Order实体转换为OrderResponseDTO
     * 
     * @param order Order实体
     * @return OrderResponseDTO
     */
    public static OrderResponseDTO fromOrder(Order order) {
        if (order == null) {
            return null;
        }
        
        // 转换订单字段为Map
        Map<String, String> fieldMap = order.getFields().stream()
                .collect(Collectors.toMap(OrderField::getFieldKey, OrderField::getFieldValue));
        
        // 转换订单日志
        List<OrderLogSimpleDTO> logList = order.getLogs().stream()
                .map(OrderLogSimpleDTO::fromOrderLog)
                .collect(Collectors.toList());
        
        return OrderResponseDTO.builder()
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
                .build();
    }
    
    /**
     * 将Order实体列表转换为OrderResponseDTO列表
     * 
     * @param orders Order实体列表
     * @return OrderResponseDTO列表
     */
    public static List<OrderResponseDTO> fromOrders(List<Order> orders) {
        if (orders == null) {
            return null;
        }
        
        return orders.stream()
                .map(OrderResponseDTO::fromOrder)
                .collect(Collectors.toList());
    }
} 