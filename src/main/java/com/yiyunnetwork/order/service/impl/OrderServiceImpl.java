package com.yiyunnetwork.order.service.impl;

import com.yiyunnetwork.order.dto.order.OrderCreateDTO;
import com.yiyunnetwork.order.dto.order.OrderStatusUpdateDTO;
import com.yiyunnetwork.order.exception.BusinessException;
import com.yiyunnetwork.order.model.Order;
import com.yiyunnetwork.order.model.OrderField;
import com.yiyunnetwork.order.model.OrderLog;
import com.yiyunnetwork.order.model.Product;
import com.yiyunnetwork.order.model.ProductField;
import com.yiyunnetwork.order.model.User;
import com.yiyunnetwork.order.model.enums.OrderStatus;
import com.yiyunnetwork.order.repository.OrderFieldRepository;
import com.yiyunnetwork.order.repository.OrderLogRepository;
import com.yiyunnetwork.order.repository.OrderRepository;
import com.yiyunnetwork.order.repository.ProductFieldRepository;
import com.yiyunnetwork.order.repository.ProductRepository;
import com.yiyunnetwork.order.service.EmailService;
import com.yiyunnetwork.order.service.OrderService;
import com.yiyunnetwork.order.util.OrderUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderFieldRepository orderFieldRepository;
    private final OrderLogRepository orderLogRepository;
    private final ProductRepository productRepository;
    private final ProductFieldRepository productFieldRepository;
    private final EmailService emailService;
    @Qualifier("taskExecutor")
    private final TaskExecutor taskExecutor;
    private final OrderUtils orderUtils;

    @Override
    public Order createOrder(OrderCreateDTO orderCreateDTO) {
        // 获取商品
        Product product = productRepository.findById(orderCreateDTO.getProductId())
                .orElseThrow(() -> new BusinessException("商品不存在"));
        
        if (!product.getEnabled()) {
            throw new BusinessException("该商品已下架");
        }
        
        // 获取商品字段
        List<ProductField> productFields = productFieldRepository.findByProductOrderByDisplayOrderAsc(product);
        
        // 检查必填字段
        for (ProductField field : productFields) {
            if (field.getRequired() && 
                    (!orderCreateDTO.getFields().containsKey(field.getFieldKey()) || 
                     orderCreateDTO.getFields().get(field.getFieldKey()).isBlank())) {
                throw new BusinessException("字段 " + field.getName() + " 为必填项");
            }
        }
        
        // 创建订单
        Order order = Order.builder()
                .product(product)
                .customerEmail(orderCreateDTO.getCustomerEmail())
                .customerPhone(orderCreateDTO.getCustomerPhone())
                .totalAmount(product.getPrice())
                .status(OrderStatus.PENDING_AGENT_REVIEW)
                .orderNo(orderUtils.generateOrderNo())
                .accessToken(orderUtils.generateAccessToken())
                .fields(new ArrayList<>())
                .logs(new ArrayList<>())
                .build();
        
        Order savedOrder = orderRepository.save(order);
        
        // 保存订单字段
        List<OrderField> orderFields = new ArrayList<>();
        for (ProductField field : productFields) {
            if (orderCreateDTO.getFields().containsKey(field.getFieldKey())) {
                OrderField orderField = OrderField.builder()
                        .order(savedOrder)
                        .fieldKey(field.getFieldKey())
                        .fieldType(field.getFieldType())
                        .fieldValue(orderCreateDTO.getFields().get(field.getFieldKey()))
                        .build();
                orderFields.add(orderField);
            }
        }
        
        orderFieldRepository.saveAll(orderFields);
        
        // 记录订单日志
        OrderLog orderLog = OrderLog.builder()
                .order(savedOrder)
                .operatorEmail(orderCreateDTO.getCustomerEmail())
                .operatorName("客户")
                .fromStatus(null)
                .toStatus(OrderStatus.PENDING_AGENT_REVIEW)
                .remark("订单创建")
                .build();
        
        orderLogRepository.save(orderLog);
        
        // 异步发送邮件通知
        final String customerEmail = savedOrder.getCustomerEmail();
        final String orderNo = savedOrder.getOrderNo();
        final String orderId = savedOrder.getId().toString();
        
        taskExecutor.execute(() -> {
            try {
                // 构建订单访问链接
                String orderLink = orderUtils.generateOrderLink(savedOrder);
                
                emailService.sendOrderCreationNotification(
                    customerEmail,
                    orderNo,
                    orderLink
                );
            } catch (Exception e) {
                log.error("Failed to send order creation notification email for order: " + orderId, e);
            }
        });
        
        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("订单不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException("订单不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderByIdAndToken(UUID id, String token) {
        return orderRepository.findByIdAndAccessToken(id, token)
                .orElseThrow(() -> new BusinessException("订单不存在或访问令牌无效"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByCustomerEmail(String email, Pageable pageable) {
        return orderRepository.findByCustomerEmail(email, pageable);
    }

    @Override
    public Order updateOrderStatus(UUID id, OrderStatusUpdateDTO orderStatusUpdateDTO, User operator) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        
        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = orderStatusUpdateDTO.getStatus();
        
        if (oldStatus == newStatus) {
            return order;
        }
        
        // 检查状态变更的合法性
        checkStatusTransitionValidity(oldStatus, newStatus);
        
        order.setStatus(newStatus);
        order.setRemark(orderStatusUpdateDTO.getRemark());
        
        // 根据不同的状态变更，设置代理或超管
        if (newStatus == OrderStatus.PENDING_ADMIN_REVIEW && oldStatus == OrderStatus.PENDING_AGENT_REVIEW) {
            order.setAgent(operator);
        } else if ((newStatus == OrderStatus.IN_PROGRESS || newStatus == OrderStatus.REJECTED) 
                && oldStatus == OrderStatus.PENDING_ADMIN_REVIEW) {
            order.setAdmin(operator);
        }
        
        // 记录日志
        OrderLog orderLog = OrderLog.builder()
                .order(order)
                .operator(operator)
                .operatorName(operator.getUsername())
                .operatorEmail(operator.getEmail())
                .fromStatus(oldStatus)
                .toStatus(newStatus)
                .remark(orderStatusUpdateDTO.getRemark())
                .build();
        
        orderLogRepository.save(orderLog);
        
        Order updatedOrder = orderRepository.save(order);
        
        // 异步发送邮件通知，避免阻塞响应
        final UUID orderId = updatedOrder.getId();
        final String customerEmail = updatedOrder.getCustomerEmail();
        final String orderNo = updatedOrder.getOrderNo();
        final String statusName = newStatus.name();
        final String remark = orderStatusUpdateDTO.getRemark();
        
        taskExecutor.execute(() -> {
            try {
                // 使用OrderUtils生成订单访问链接
                String orderLink = orderUtils.generateOrderLink(updatedOrder);
                
                emailService.sendOrderStatusChangeNotification(
                    customerEmail,
                    orderNo,
                    statusName,
                    remark,
                    orderLink
                );
            } catch (Exception e) {
                log.error("Failed to send order status change notification email for order: " + orderId, e);
            }
        });
        
        return updatedOrder;
    }

    @Override
    public Order agentReviewOrder(UUID id, boolean approved, User agent, String remark) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        
        if (order.getStatus() != OrderStatus.PENDING_AGENT_REVIEW) {
            throw new BusinessException("只能审核待代理审核状态的订单");
        }
        
        OrderStatusUpdateDTO statusUpdate = new OrderStatusUpdateDTO();
        if (approved) {
            statusUpdate.setStatus(OrderStatus.PENDING_ADMIN_REVIEW);
            statusUpdate.setRemark(remark != null ? remark : "代理审核通过");
        } else {
            statusUpdate.setStatus(OrderStatus.REJECTED);
            statusUpdate.setRemark(remark != null ? remark : "代理审核不通过");
        }
        
        return updateOrderStatus(id, statusUpdate, agent);
    }

    @Override
    public Order adminReviewOrder(UUID id, boolean approved, User admin, String remark) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        
        if (order.getStatus() != OrderStatus.PENDING_ADMIN_REVIEW) {
            throw new BusinessException("只能审核待超管审核状态的订单");
        }
        
        OrderStatusUpdateDTO statusUpdate = new OrderStatusUpdateDTO();
        if (approved) {
            statusUpdate.setStatus(OrderStatus.IN_PROGRESS);
            statusUpdate.setRemark(remark != null ? remark : "超管审核通过");
        } else {
            statusUpdate.setStatus(OrderStatus.REJECTED);
            statusUpdate.setRemark(remark != null ? remark : "超管审核不通过");
        }
        
        return updateOrderStatus(id, statusUpdate, admin);
    }

    @Override
    public Order markOrderInProgress(UUID id, User admin, String remark) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        
        if (order.getStatus() != OrderStatus.PENDING_ADMIN_REVIEW) {
            throw new BusinessException("只能将待超管审核状态的订单标记为执行中");
        }
        
        OrderStatusUpdateDTO statusUpdate = new OrderStatusUpdateDTO();
        statusUpdate.setStatus(OrderStatus.IN_PROGRESS);
        statusUpdate.setRemark(remark != null ? remark : "开始执行订单");
        
        return updateOrderStatus(id, statusUpdate, admin);
    }

    @Override
    public Order completeOrder(UUID id, User admin, String remark) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        
        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new BusinessException("只能将执行中状态的订单标记为已完成");
        }
        
        OrderStatusUpdateDTO statusUpdate = new OrderStatusUpdateDTO();
        statusUpdate.setStatus(OrderStatus.COMPLETED);
        statusUpdate.setRemark(remark != null ? remark : "订单完成");
        
        return updateOrderStatus(id, statusUpdate, admin);
    }

    @Override
    public Order cancelOrder(UUID id, String token, String remark) {
        Order order = orderRepository.findByIdAndAccessToken(id, token)
                .orElseThrow(() -> new BusinessException("订单不存在或访问令牌无效"));
        
        if (order.getStatus() != OrderStatus.PENDING_AGENT_REVIEW) {
            throw new BusinessException("只能取消待代理审核状态的订单");
        }
        
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        order.setRemark(remark);
        
        // 记录日志
        OrderLog orderLog = OrderLog.builder()
                .order(order)
                .operatorName("客户")
                .operatorEmail(order.getCustomerEmail())
                .fromStatus(oldStatus)
                .toStatus(OrderStatus.CANCELLED)
                .remark(remark != null ? remark : "客户取消订单")
                .build();
        
        orderLogRepository.save(orderLog);
        
        Order updatedOrder = orderRepository.save(order);
        
        // 异步发送邮件通知
        final UUID orderId = updatedOrder.getId();
        final String customerEmail = updatedOrder.getCustomerEmail();
        final String orderNo = updatedOrder.getOrderNo();
        final String remarkText = remark;
        
        taskExecutor.execute(() -> {
            try {
                // 使用OrderUtils生成订单访问链接
                String orderLink = orderUtils.generateOrderLink(updatedOrder);
                
                emailService.sendOrderStatusChangeNotification(
                    customerEmail,
                    orderNo,
                    OrderStatus.CANCELLED.name(),
                    remarkText,
                    orderLink
                );
            } catch (Exception e) {
                log.error("Failed to send order cancellation notification email for order: " + orderId, e);
            }
        });
        
        return updatedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderLog> getOrderLogs(UUID orderId, Pageable pageable) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        
        return orderLogRepository.findByOrderOrderByOperateTimeDesc(order, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getOrderFields(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        
        List<OrderField> orderFields = orderFieldRepository.findByOrder(order);
        
        Map<String, String> fieldMap = new HashMap<>();
        for (OrderField field : orderFields) {
            fieldMap.put(field.getFieldKey(), field.getFieldValue());
        }
        
        return fieldMap;
    }
    
    /**
     * 检查订单状态变更的合法性
     */
    private void checkStatusTransitionValidity(OrderStatus oldStatus, OrderStatus newStatus) {
        if (oldStatus == OrderStatus.COMPLETED || oldStatus == OrderStatus.CANCELLED) {
            throw new BusinessException("已完成或已取消的订单不能更改状态");
        }
        
        switch (oldStatus) {
            case PENDING_AGENT_REVIEW:
                if (newStatus != OrderStatus.PENDING_ADMIN_REVIEW && newStatus != OrderStatus.REJECTED && newStatus != OrderStatus.CANCELLED) {
                    throw new BusinessException("待代理审核的订单只能变更为待超管审核、已退回或已取消状态");
                }
                break;
            case PENDING_ADMIN_REVIEW:
                if (newStatus != OrderStatus.IN_PROGRESS && newStatus != OrderStatus.REJECTED && newStatus != OrderStatus.PENDING_AGENT_REVIEW) {
                    throw new BusinessException("待超管审核的订单只能变更为执行中、已退回或退回到待代理审核状态");
                }
                break;
            case IN_PROGRESS:
                if (newStatus != OrderStatus.COMPLETED) {
                    throw new BusinessException("执行中的订单只能变更为已完成状态");
                }
                break;
            case REJECTED:
                throw new BusinessException("已退回的订单不能更改状态");
            default:
                break;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByAgent(User agent, Pageable pageable) {
        return orderRepository.findByAgent(agent, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByAgentAndStatus(User agent, OrderStatus status, Pageable pageable) {
        return orderRepository.findByAgentAndStatus(agent, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByAdmin(User admin, Pageable pageable) {
        return orderRepository.findByAdmin(admin, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByAdminAndStatus(User admin, OrderStatus status, Pageable pageable) {
        return orderRepository.findByAdminAndStatus(admin, status, pageable);
    }
} 