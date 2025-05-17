package com.yiyunnetwork.order.model.enums;

/**
 * 订单状态枚举
 */
public enum OrderStatus {
    /**
     * 待代理审核
     */
    PENDING_AGENT_REVIEW,
    
    /**
     * 待超管审核
     */
    PENDING_ADMIN_REVIEW,
    
    /**
     * 订单进行中（执行中）
     */
    IN_PROGRESS,
    
    /**
     * 已完成
     */
    COMPLETED,
    
    /**
     * 已退回（拒绝）
     */
    REJECTED,
    
    /**
     * 已取消（用户取消）
     */
    CANCELLED
} 