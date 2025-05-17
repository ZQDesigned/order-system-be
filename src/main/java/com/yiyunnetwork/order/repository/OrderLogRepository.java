package com.yiyunnetwork.order.repository;

import com.yiyunnetwork.order.model.Order;
import com.yiyunnetwork.order.model.OrderLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderLogRepository extends JpaRepository<OrderLog, Long> {

    List<OrderLog> findByOrderOrderByOperateTimeDesc(Order order);
    
    Page<OrderLog> findByOrderOrderByOperateTimeDesc(Order order, Pageable pageable);
} 