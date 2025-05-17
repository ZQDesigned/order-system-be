package com.yiyunnetwork.order.controller;

import com.yiyunnetwork.order.dto.ApiResult;
import com.yiyunnetwork.order.dto.user.UserDTO;
import com.yiyunnetwork.order.dto.user.UserResponseDTO;
import com.yiyunnetwork.order.exception.BusinessException;
import com.yiyunnetwork.order.model.User;
import com.yiyunnetwork.order.service.UserService;
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

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "用户管理", description = "提供用户的增删改查、角色分配等接口")
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取所有用户", description = "分页获取所有用户")
    @GetMapping
    public ApiResult<Page<UserResponseDTO>> getAllUsers(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<User> userPage = userService.findAll(pageable);
        
        // 将Page<User>转换为Page<UserResponseDTO>，确保不返回密码信息
        Page<UserResponseDTO> userResponsePage = userPage.map(UserResponseDTO::fromUser);
        return ApiResult.success(userResponsePage);
    }

    @Operation(summary = "获取用户详情", description = "根据ID获取用户详情")
    @GetMapping("/{id}")
    public ApiResult<UserResponseDTO> getUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new BusinessException("用户不存在"));
            return ApiResult.success(UserResponseDTO.fromUser(user));
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "创建用户", description = "创建新用户")
    @PostMapping
    public ApiResult<UserResponseDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        try {
            User user = userService.createUser(userDTO);
            return ApiResult.success(UserResponseDTO.fromUser(user));
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "更新用户", description = "更新用户信息")
    @PutMapping("/{id}")
    public ApiResult<User> updateUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {
        try {
            return ApiResult.success(userService.updateUser(id, userDTO));
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "删除用户", description = "根据ID删除用户")
    @DeleteMapping("/{id}")
    public ApiResult<Void> deleteUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ApiResult.success();
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "给用户添加角色", description = "为指定用户添加角色")
    @PostMapping("/{id}/roles")
    public ApiResult<User> addRoleToUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "角色名称", required = true) @RequestParam String roleName) {
        try {
            return ApiResult.success(userService.addRoleToUser(id, roleName));
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "从用户移除角色", description = "从指定用户移除角色")
    @DeleteMapping("/{id}/roles")
    public ApiResult<User> removeRoleFromUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "角色名称", required = true) @RequestParam String roleName) {
        try {
            return ApiResult.success(userService.removeRoleFromUser(id, roleName));
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "修改用户密码", description = "修改指定用户的密码")
    @PutMapping("/{id}/password")
    public ApiResult<Void> changePassword(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "原密码", required = true) @RequestParam String oldPassword,
            @Parameter(description = "新密码", required = true) @RequestParam String newPassword) {
        try {
            userService.changePassword(id, oldPassword, newPassword);
            return ApiResult.success();
        } catch (BusinessException e) {
            return ApiResult.failed(e.getMessage());
        }
    }
} 