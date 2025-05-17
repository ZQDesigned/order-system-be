package com.yiyunnetwork.order.repository;

import com.yiyunnetwork.order.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByEnabled(Boolean enabled);
    
    Page<Product> findByEnabled(Boolean enabled, Pageable pageable);
    
    Page<Product> findByNameContainingAndEnabled(String name, Boolean enabled, Pageable pageable);
} 