package com.yiyunnetwork.order.service;

import com.yiyunnetwork.order.dto.user.UserDTO;
import com.yiyunnetwork.order.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

public interface UserService extends UserDetailsService {

    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);
    
    /**
     * 根据ID查找用户
     */
    Optional<User> findById(Long id);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 创建新用户
     */
    User createUser(UserDTO userDTO);
    
    /**
     * 更新用户信息
     */
    User updateUser(Long id, UserDTO userDTO);
    
    /**
     * 修改用户密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 分页查询用户
     */
    Page<User> findAll(Pageable pageable);
    
    /**
     * 删除用户
     */
    void deleteUser(Long id);
    
    /**
     * 为用户添加角色
     */
    User addRoleToUser(Long userId, String roleName);
    
    /**
     * 从用户中移除角色
     */
    User removeRoleFromUser(Long userId, String roleName);
    
    /**
     * 保存用户
     */
    User save(User user);
    
    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
} 