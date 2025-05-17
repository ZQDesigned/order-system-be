package com.yiyunnetwork.order.repository;

import com.yiyunnetwork.order.model.Product;
import com.yiyunnetwork.order.model.ProductField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductFieldRepository extends JpaRepository<ProductField, Long> {

    List<ProductField> findByProductOrderByDisplayOrderAsc(Product product);
    
    Optional<ProductField> findByProductAndFieldKey(Product product, String fieldKey);
    
    void deleteByProduct(Product product);
} 