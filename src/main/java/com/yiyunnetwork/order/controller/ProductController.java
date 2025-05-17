package com.yiyunnetwork.order.controller;

import com.yiyunnetwork.order.dto.ApiResult;
import com.yiyunnetwork.order.dto.product.ProductDTO;
import com.yiyunnetwork.order.exception.BusinessException;
import com.yiyunnetwork.order.model.Product;
import com.yiyunnetwork.order.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "商品管理", description = "提供商品及商品字段的增删改查接口")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "获取所有启用的商品", description = "获取所有已启用的商品列表，不需要登录")
    @GetMapping("/public/enabled")
    public ApiResult<List<Product>> getEnabledProducts() {
        return ApiResult.success(productService.getEnabledProducts());
    }

    @Operation(summary = "获取商品详情", description = "根据ID获取商品详情，包含商品字段")
    @GetMapping("/public/{id}")
    public ApiResult<Product> getProduct(
            @Parameter(description = "商品ID", required = true) @PathVariable Long id) {
        try {
            return ApiResult.success(productService.getProduct(id));
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "分页获取启用的商品", description = "分页获取所有已启用的商品")
    @GetMapping("/public")
    public ApiResult<Page<Product>> getEnabledProductsPage(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        
        if (keyword != null && !keyword.isBlank()) {
            return ApiResult.success(productService.searchEnabledProducts(keyword, pageable));
        } else {
            return ApiResult.success(productService.getEnabledProducts(pageable));
        }
    }

    @Operation(summary = "创建商品", description = "创建新商品及其字段配置")
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Product> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        try {
            return ApiResult.success(productService.createProduct(productDTO));
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "更新商品", description = "更新商品及其字段配置")
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Product> updateProduct(
            @Parameter(description = "商品ID", required = true) @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {
        try {
            return ApiResult.success(productService.updateProduct(id, productDTO));
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "删除商品", description = "删除商品及其字段配置")
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Void> deleteProduct(
            @Parameter(description = "商品ID", required = true) @PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ApiResult.success();
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "启用或禁用商品", description = "修改商品的启用状态")
    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Product> toggleProductStatus(
            @Parameter(description = "商品ID", required = true) @PathVariable Long id,
            @Parameter(description = "启用状态", required = true) @RequestParam boolean enabled) {
        try {
            return ApiResult.success(productService.toggleProductStatus(id, enabled));
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "分页获取所有商品", description = "管理员获取所有商品，包括已禁用的")
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Page<Product>> getAllProducts(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ApiResult.success(productService.getAllProducts(pageable));
    }
} 