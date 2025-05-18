package com.yiyunnetwork.order.controller;

import com.yiyunnetwork.order.dto.ApiResult;
import com.yiyunnetwork.order.dto.order.OrderCreateDTO;
import com.yiyunnetwork.order.dto.order.OrderStatusUpdateDTO;
import com.yiyunnetwork.order.dto.order.OrderResponseDTO;
import com.yiyunnetwork.order.dto.order.OrderDetailResponseDTO;
import com.yiyunnetwork.order.exception.BusinessException;
import com.yiyunnetwork.order.model.Order;
import com.yiyunnetwork.order.model.OrderLog;
import com.yiyunnetwork.order.model.User;
import com.yiyunnetwork.order.model.enums.OrderStatus;
import com.yiyunnetwork.order.service.OrderService;
import com.yiyunnetwork.order.service.UserService;
import com.yiyunnetwork.order.service.VerificationCodeService;
import com.yiyunnetwork.order.util.OrderUtils;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "订单管理", description = "提供订单的创建、查询、审核等接口")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final VerificationCodeService verificationCodeService;
    private final OrderUtils orderUtils;

    @Operation(summary = "创建订单", description = "客户创建新订单，可以选择性地指定代理ID")
    @PostMapping("/public")
    public ApiResult<OrderResponseDTO> createOrder(@Valid @RequestBody OrderCreateDTO orderCreateDTO) {
        try {
            Order order = orderService.createOrder(orderCreateDTO);
            return ApiResult.success(OrderResponseDTO.fromOrder(order));
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "根据ID和令牌获取订单", description = "客户根据订单ID和访问令牌获取订单详情")
    @GetMapping("/public/{id}")
    public ApiResult<OrderResponseDTO> getOrderByIdAndToken(
            @Parameter(description = "订单ID", required = true) @PathVariable UUID id,
            @Parameter(description = "访问令牌", required = true) @RequestParam String token) {
        try {
            Order order = orderService.getOrderByIdAndToken(id, token);
            return ApiResult.success(OrderResponseDTO.fromOrder(order));
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "取消订单", description = "客户取消未审核的订单")
    @PostMapping("/public/{id}/cancel")
    public ApiResult<OrderResponseDTO> cancelOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable UUID id,
            @Parameter(description = "访问令牌", required = true) @RequestParam String token,
            @Parameter(description = "验证码", required = true) @RequestParam String code,
            @Parameter(description = "取消原因") @RequestParam(required = false) String remark) {
        try {
            // 获取订单
            Order order = orderService.getOrderByIdAndToken(id, token);
            
            // 验证验证码
            if (!verificationCodeService.verifyCode(order.getCustomerEmail(), code)) {
                return ApiResult.failed("验证码无效或已过期");
            }
            
            // 标记验证码为已使用
            verificationCodeService.markCodeAsUsed(order.getCustomerEmail(), code);
            
            // 取消订单
            Order canceledOrder = orderService.cancelOrder(id, token, remark);
            return ApiResult.success(OrderResponseDTO.fromOrder(canceledOrder));
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取订单字段值", description = "根据订单ID获取所有订单字段值")
    @GetMapping("/public/{id}/fields")
    public ApiResult<Map<String, String>> getOrderFields(
            @Parameter(description = "订单ID", required = true) @PathVariable UUID id,
            @Parameter(description = "访问令牌", required = true) @RequestParam String token) {
        try {
            // 验证订单访问权限
            orderService.getOrderByIdAndToken(id, token);
            
            return ApiResult.success(orderService.getOrderFields(id));
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取客户订单列表", description = "客户根据邮箱和验证码获取自己的订单列表")
    @GetMapping("/public/customer")
    public ApiResult<Page<OrderResponseDTO>> getCustomerOrders(
            @Parameter(description = "客户邮箱", required = true) @RequestParam String email,
            @Parameter(description = "验证码", required = true) @RequestParam String code,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            // 验证验证码
            if (!verificationCodeService.verifyCode(email, code)) {
                return ApiResult.failed("验证码无效或已过期");
            }
            
            // 标记验证码为已使用
            verificationCodeService.markCodeAsUsed(email, code);
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
            Page<Order> orderPage = orderService.getOrdersByCustomerEmail(email, pageable);
            
            // 转换为OrderResponseDTO
            Page<OrderResponseDTO> responsePage = orderPage.map(OrderResponseDTO::fromOrder);
            
            return ApiResult.success(responsePage);
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取订单二维码内容", description = "根据订单ID获取订单二维码内容，用于前端生成二维码")
    @GetMapping("/public/{id}/qrcode")
    public ApiResult<String> getOrderQrCodeContent(
            @Parameter(description = "订单ID", required = true) @PathVariable UUID id,
            @Parameter(description = "访问令牌", required = true) @RequestParam String token) {
        try {
            // 验证订单访问权限
            Order order = orderService.getOrderByIdAndToken(id, token);
            
            // 使用OrderUtils生成二维码内容
            String qrCodeContent = orderUtils.generateQrCodeContent(order);
            
            return ApiResult.success(qrCodeContent);
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }

    // 管理员和代理商接口
    
    @Operation(summary = "获取所有订单", description = "管理员获取所有订单，代理只能获取自己负责的订单")
    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ApiResult<Page<OrderDetailResponseDTO>> getAllOrders(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<Order> orderPage;
        
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(authentication.getName());
        
        // 根据用户角色决定查看哪些订单
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            // 超管可以看到所有订单
            orderPage = orderService.getAllOrders(pageable);
        } else {
            // 代理只能看到自己负责的订单
            orderPage = orderService.getOrdersByAgent(currentUser, pageable);
        }
        
        // 转换为OrderDetailResponseDTO
        Page<OrderDetailResponseDTO> responsePage = orderPage.map(OrderDetailResponseDTO::fromOrder);
        
        return ApiResult.success(responsePage);
    }

    @Operation(summary = "根据状态获取订单", description = "管理员根据状态获取订单，代理只能获取自己负责的订单")
    @GetMapping("/admin/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ApiResult<Page<OrderDetailResponseDTO>> getOrdersByStatus(
            @Parameter(description = "订单状态", required = true) @RequestParam OrderStatus status,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<Order> orderPage;
        
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(authentication.getName());
        
        // 根据用户角色决定查看哪些订单
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            // 超管可以看到所有订单
            orderPage = orderService.getOrdersByStatus(status, pageable);
        } else {
            // 代理只能看到自己负责的订单
            orderPage = orderService.getOrdersByAgentAndStatus(currentUser, status, pageable);
        }
        
        // 转换为OrderDetailResponseDTO
        Page<OrderDetailResponseDTO> responsePage = orderPage.map(OrderDetailResponseDTO::fromOrder);
        
        return ApiResult.success(responsePage);
    }

    @Operation(summary = "获取订单详情", description = "管理员根据ID获取订单详情，代理只能获取自己负责的订单")
    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ApiResult<OrderDetailResponseDTO> getOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable UUID id) {
        try {
            Order order = orderService.getOrder(id);
            
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByUsername(authentication.getName());
            
            // 检查权限：管理员可以查看所有订单，代理只能查看自己负责的订单
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin && (order.getAgent() == null || !order.getAgent().getId().equals(currentUser.getId()))) {
                return ApiResult.failed(ApiResult.ResultCode.FORBIDDEN, "您没有权限查看该订单");
            }
            
            return ApiResult.success(OrderDetailResponseDTO.fromOrder(order));
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取订单日志", description = "管理员获取订单操作日志，代理只能查看自己负责的订单日志")
    @GetMapping("/admin/{id}/logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ApiResult<Page<OrderLog>> getOrderLogs(
            @Parameter(description = "订单ID", required = true) @PathVariable UUID id,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Order order = orderService.getOrder(id);
            
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByUsername(authentication.getName());
            
            // 检查权限：管理员可以查看所有订单，代理只能查看自己负责的订单
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin && (order.getAgent() == null || !order.getAgent().getId().equals(currentUser.getId()))) {
                return ApiResult.failed(ApiResult.ResultCode.FORBIDDEN, "您没有权限查看该订单日志");
            }
            
            Pageable pageable = PageRequest.of(page, size);
            return ApiResult.success(orderService.getOrderLogs(id, pageable));
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "代理审核订单", description = "代理审核待代理审核状态的订单，只能审核自己负责的订单")
    @PostMapping("/admin/{id}/agent-review")
    @PreAuthorize("hasRole('AGENT')")
    public ApiResult<OrderDetailResponseDTO> agentReviewOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable UUID id,
            @Parameter(description = "是否通过", required = true) @RequestParam boolean approved,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User agent = userService.findByUsername(authentication.getName());
            
            // 获取订单
            Order order = orderService.getOrder(id);
            
            // 检查订单状态
            if (order.getStatus() != OrderStatus.PENDING_AGENT_REVIEW) {
                return ApiResult.failed("只能审核待代理审核状态的订单");
            }
            
            // 检查该订单是否已经分配给当前代理
            if (order.getAgent() != null && !order.getAgent().getId().equals(agent.getId())) {
                return ApiResult.failed(ApiResult.ResultCode.FORBIDDEN, "您没有权限审核该订单");
            }
            
            Order reviewedOrder = orderService.agentReviewOrder(id, approved, agent, remark);
            return ApiResult.success(OrderDetailResponseDTO.fromOrder(reviewedOrder));
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "超管审核订单", description = "超管审核待超管审核状态的订单")
    @PostMapping("/admin/{id}/admin-review")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<OrderDetailResponseDTO> adminReviewOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable UUID id,
            @Parameter(description = "是否通过", required = true) @RequestParam boolean approved,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User admin = userService.findByUsername(authentication.getName());
            
            Order order = orderService.adminReviewOrder(id, approved, admin, remark);
            return ApiResult.success(OrderDetailResponseDTO.fromOrder(order));
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "标记为执行中", description = "超管将订单标记为执行中")
    @PostMapping("/admin/{id}/in-progress")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<OrderDetailResponseDTO> markOrderInProgress(
            @Parameter(description = "订单ID", required = true) @PathVariable UUID id,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User admin = userService.findByUsername(authentication.getName());
            
            Order order = orderService.markOrderInProgress(id, admin, remark);
            return ApiResult.success(OrderDetailResponseDTO.fromOrder(order));
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "标记为已完成", description = "超管将订单标记为已完成")
    @PostMapping("/admin/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<OrderDetailResponseDTO> completeOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable UUID id,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User admin = userService.findByUsername(authentication.getName());
            
            Order order = orderService.completeOrder(id, admin, remark);
            return ApiResult.success(OrderDetailResponseDTO.fromOrder(order));
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "更新订单状态", description = "管理员更新订单状态")
    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ApiResult<OrderDetailResponseDTO> updateOrderStatus(
            @Parameter(description = "订单ID", required = true) @PathVariable UUID id,
            @Valid @RequestBody OrderStatusUpdateDTO orderStatusUpdateDTO) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User operator = userService.findByUsername(authentication.getName());
            
            Order order = orderService.updateOrderStatus(id, orderStatusUpdateDTO, operator);
            return ApiResult.success(OrderDetailResponseDTO.fromOrder(order));
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }
} 