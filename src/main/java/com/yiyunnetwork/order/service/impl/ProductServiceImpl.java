package com.yiyunnetwork.order.service.impl;

import com.yiyunnetwork.order.dto.product.ProductDTO;
import com.yiyunnetwork.order.dto.product.ProductFieldDTO;
import com.yiyunnetwork.order.exception.BusinessException;
import com.yiyunnetwork.order.model.Product;
import com.yiyunnetwork.order.model.ProductField;
import com.yiyunnetwork.order.repository.ProductFieldRepository;
import com.yiyunnetwork.order.repository.ProductRepository;
import com.yiyunnetwork.order.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductFieldRepository productFieldRepository;

    @Override
    public Product createProduct(ProductDTO productDTO) {
        Product product = Product.builder()
                .name(productDTO.getName())
                .description(productDTO.getDescription())
                .price(productDTO.getPrice())
                .enabled(productDTO.getEnabled())
                .imageUrl(productDTO.getImageUrl())
                .fields(new ArrayList<>())
                .build();

        Product savedProduct = productRepository.save(product);
        
        // 保存商品字段
        if (productDTO.getFields() != null && !productDTO.getFields().isEmpty()) {
            List<ProductField> fields = productDTO.getFields().stream()
                    .map(fieldDTO -> mapToProductField(fieldDTO, savedProduct))
                    .collect(Collectors.toList());
            
            productFieldRepository.saveAll(fields);
        }
        
        return savedProduct;
    }

    @Override
    public Product updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setEnabled(productDTO.getEnabled());
        product.setImageUrl(productDTO.getImageUrl());
        
        // 先删除所有原有字段
        productFieldRepository.deleteByProduct(product);
        
        // 再保存新字段
        if (productDTO.getFields() != null && !productDTO.getFields().isEmpty()) {
            List<ProductField> fields = productDTO.getFields().stream()
                    .map(fieldDTO -> mapToProductField(fieldDTO, product))
                    .collect(Collectors.toList());
            
            productFieldRepository.saveAll(fields);
        }
        
        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        
        // 先删除关联的字段
        productFieldRepository.deleteByProduct(product);
        
        // 再删除商品
        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getEnabledProducts() {
        return productRepository.findByEnabled(true);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getEnabledProducts(Pageable pageable) {
        return productRepository.findByEnabled(true, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> searchEnabledProducts(String name, Pageable pageable) {
        return productRepository.findByNameContainingAndEnabled(name, true, pageable);
    }

    @Override
    public Product toggleProductStatus(Long id, boolean enabled) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        
        product.setEnabled(enabled);
        return productRepository.save(product);
    }
    
    /**
     * 将DTO转换为实体
     */
    private ProductField mapToProductField(ProductFieldDTO dto, Product product) {
        return ProductField.builder()
                .product(product)
                .name(dto.getName())
                .fieldKey(dto.getFieldKey())
                .fieldType(dto.getFieldType())
                .required(dto.getRequired())
                .validationRule(dto.getValidationRule())
                .validationMessage(dto.getValidationMessage())
                .placeholder(dto.getPlaceholder())
                .options(dto.getOptions())
                .displayOrder(dto.getDisplayOrder())
                .build();
    }
} 