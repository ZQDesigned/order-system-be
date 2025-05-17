package com.yiyunnetwork.order.repository;

import com.yiyunnetwork.order.model.Order;
import com.yiyunnetwork.order.model.OrderField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderFieldRepository extends JpaRepository<OrderField, Long> {

    List<OrderField> findByOrder(Order order);
    
    Optional<OrderField> findByOrderAndFieldKey(Order order, String fieldKey);
    
    void deleteByOrder(Order order);
} 