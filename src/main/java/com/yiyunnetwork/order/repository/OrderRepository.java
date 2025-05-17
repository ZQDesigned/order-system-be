package com.yiyunnetwork.order.repository;

import com.yiyunnetwork.order.model.Order;
import com.yiyunnetwork.order.model.User;
import com.yiyunnetwork.order.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByIdAndAccessToken(UUID id, String accessToken);
    
    Optional<Order> findByOrderNo(String orderNo);
    
    List<Order> findByStatus(OrderStatus status);
    
    List<Order> findByCustomerEmail(String email);
    
    Page<Order> findByCustomerEmail(String email, Pageable pageable);
    
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    
    Page<Order> findByAgent(User agent, Pageable pageable);
    
    Page<Order> findByAgentAndStatus(User agent, OrderStatus status, Pageable pageable);
    
    Page<Order> findByAdmin(User admin, Pageable pageable);
    
    Page<Order> findByAdminAndStatus(User admin, OrderStatus status, Pageable pageable);
    
    Page<Order> findByOrderNoContainingOrCustomerEmailContaining(
            String orderNo, String email, Pageable pageable);
} 