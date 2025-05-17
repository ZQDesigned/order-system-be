package com.yiyunnetwork.order.service;

import com.yiyunnetwork.order.dto.order.OrderCreateDTO;
import com.yiyunnetwork.order.dto.order.OrderStatusUpdateDTO;
import com.yiyunnetwork.order.model.Order;
import com.yiyunnetwork.order.model.OrderLog;
import com.yiyunnetwork.order.model.User;
import com.yiyunnetwork.order.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 订单服务接口
 */
public interface OrderService {
    
    /**
     * 创建订单
     * 
     * @param orderCreateDTO 订单创建信息
     * @return 创建后的订单
     */
    Order createOrder(OrderCreateDTO orderCreateDTO);
    
    /**
     * 获取订单详情
     * 
     * @param id 订单ID
     * @return 订单信息
     */
    Order getOrder(UUID id);
    
    /**
     * 根据订单号获取订单
     * 
     * @param orderNo 订单号
     * @return 订单信息
     */
    Order getOrderByOrderNo(String orderNo);
    
    /**
     * 根据ID和访问令牌获取订单
     * 
     * @param id    订单ID
     * @param token 访问令牌
     * @return 订单信息
     */
    Order getOrderByIdAndToken(UUID id, String token);
    
    /**
     * 根据状态获取订单列表
     * 
     * @param status 订单状态
     * @return 订单列表
     */
    List<Order> getOrdersByStatus(OrderStatus status);
    
    /**
     * 分页获取订单
     * 
     * @param pageable 分页信息
     * @return 订单分页结果
     */
    Page<Order> getAllOrders(Pageable pageable);
    
    /**
     * 根据状态分页获取订单
     * 
     * @param status   订单状态
     * @param pageable 分页信息
     * @return 订单分页结果
     */
    Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable);
    
    /**
     * 根据客户邮箱获取订单
     * 
     * @param email    客户邮箱
     * @param pageable 分页信息
     * @return 订单分页结果
     */
    Page<Order> getOrdersByCustomerEmail(String email, Pageable pageable);
    
    /**
     * 更新订单状态
     * 
     * @param id                   订单ID
     * @param orderStatusUpdateDTO 订单状态更新信息
     * @param operator             操作人
     * @return 更新后的订单
     */
    Order updateOrderStatus(UUID id, OrderStatusUpdateDTO orderStatusUpdateDTO, User operator);
    
    /**
     * 代理审核订单
     * 
     * @param id       订单ID
     * @param approved 是否通过
     * @param agent    代理
     * @param remark   备注
     * @return 更新后的订单
     */
    Order agentReviewOrder(UUID id, boolean approved, User agent, String remark);
    
    /**
     * 超管审核订单
     * 
     * @param id       订单ID
     * @param approved 是否通过
     * @param admin    超管
     * @param remark   备注
     * @return 更新后的订单
     */
    Order adminReviewOrder(UUID id, boolean approved, User admin, String remark);
    
    /**
     * 标记订单为执行中
     * 
     * @param id     订单ID
     * @param admin  超管
     * @param remark 备注
     * @return 更新后的订单
     */
    Order markOrderInProgress(UUID id, User admin, String remark);
    
    /**
     * 标记订单为已完成
     * 
     * @param id     订单ID
     * @param admin  超管
     * @param remark 备注
     * @return 更新后的订单
     */
    Order completeOrder(UUID id, User admin, String remark);
    
    /**
     * 取消订单
     * 
     * @param id     订单ID
     * @param token  访问令牌
     * @param remark 备注
     * @return 更新后的订单
     */
    Order cancelOrder(UUID id, String token, String remark);
    
    /**
     * 获取订单日志
     * 
     * @param orderId  订单ID
     * @param pageable 分页信息
     * @return 订单日志分页结果
     */
    Page<OrderLog> getOrderLogs(UUID orderId, Pageable pageable);
    
    /**
     * 获取订单字段值
     * 
     * @param orderId 订单ID
     * @return 字段值Map
     */
    Map<String, String> getOrderFields(UUID orderId);
    
    /**
     * 获取特定代理负责的订单
     * 
     * @param agent    代理用户
     * @param pageable 分页信息
     * @return 订单分页结果
     */
    Page<Order> getOrdersByAgent(User agent, Pageable pageable);
    
    /**
     * 根据状态获取特定代理负责的订单
     * 
     * @param agent    代理用户
     * @param status   订单状态
     * @param pageable 分页信息
     * @return 订单分页结果
     */
    Page<Order> getOrdersByAgentAndStatus(User agent, OrderStatus status, Pageable pageable);
    
    /**
     * 获取特定超管负责的订单
     * 
     * @param admin    超管用户
     * @param pageable 分页信息
     * @return 订单分页结果
     */
    Page<Order> getOrdersByAdmin(User admin, Pageable pageable);
    
    /**
     * 根据状态获取特定超管负责的订单
     * 
     * @param admin    超管用户
     * @param status   订单状态
     * @param pageable 分页信息
     * @return 订单分页结果
     */
    Page<Order> getOrdersByAdminAndStatus(User admin, OrderStatus status, Pageable pageable);
} 