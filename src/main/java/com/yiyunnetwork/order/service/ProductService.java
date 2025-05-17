package com.yiyunnetwork.order.service;

import com.yiyunnetwork.order.dto.product.ProductDTO;
import com.yiyunnetwork.order.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService {
    
    /**
     * 创建新商品
     * 
     * @param productDTO 商品信息
     * @return 创建后的商品
     */
    Product createProduct(ProductDTO productDTO);
    
    /**
     * 更新商品信息
     * 
     * @param id          商品ID
     * @param productDTO  商品信息
     * @return 更新后的商品
     */
    Product updateProduct(Long id, ProductDTO productDTO);
    
    /**
     * 获取商品详情
     * 
     * @param id 商品ID
     * @return 商品信息
     */
    Product getProduct(Long id);
    
    /**
     * 删除商品
     * 
     * @param id 商品ID
     */
    void deleteProduct(Long id);
    
    /**
     * 获取所有已启用商品
     * 
     * @return 商品列表
     */
    List<Product> getEnabledProducts();
    
    /**
     * 分页获取所有已启用商品
     * 
     * @param pageable 分页信息
     * @return 商品分页结果
     */
    Page<Product> getEnabledProducts(Pageable pageable);
    
    /**
     * 分页获取所有商品
     * 
     * @param pageable 分页信息
     * @return 商品分页结果
     */
    Page<Product> getAllProducts(Pageable pageable);
    
    /**
     * 根据名称搜索已启用商品
     * 
     * @param name     商品名称
     * @param pageable 分页信息
     * @return 商品分页结果
     */
    Page<Product> searchEnabledProducts(String name, Pageable pageable);
    
    /**
     * 启用或禁用商品
     * 
     * @param id      商品ID
     * @param enabled 是否启用
     * @return 更新后的商品
     */
    Product toggleProductStatus(Long id, boolean enabled);
} 