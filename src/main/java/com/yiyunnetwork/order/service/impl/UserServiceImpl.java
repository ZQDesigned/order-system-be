package com.yiyunnetwork.order.service.impl;

import com.yiyunnetwork.order.dto.user.UserDTO;
import com.yiyunnetwork.order.exception.BusinessException;
import com.yiyunnetwork.order.model.Role;
import com.yiyunnetwork.order.model.User;
import com.yiyunnetwork.order.repository.RoleRepository;
import com.yiyunnetwork.order.repository.UserRepository;
import com.yiyunnetwork.order.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User createUser(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }

        User user = User.builder()
                .username(userDTO.getUsername())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .email(userDTO.getEmail())
                .realName(userDTO.getRealName())
                .phone(userDTO.getPhone())
                .enabled(userDTO.isEnabled())
                .roles(new ArrayList<>())
                .build();

        if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
            userDTO.getRoles().forEach(roleName -> {
                roleRepository.findByName(roleName).ifPresent(user.getRoles()::add);
            });
        }

        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 检查用户名是否已存在
        if (!user.getUsername().equals(userDTO.getUsername()) &&
                userRepository.existsByUsername(userDTO.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (!user.getEmail().equals(userDTO.getEmail()) &&
                userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }

        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setRealName(userDTO.getRealName());
        user.setPhone(userDTO.getPhone());
        user.setEnabled(userDTO.isEnabled());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        if (userDTO.getRoles() != null) {
            // 清空原有角色
            user.getRoles().clear();
            
            // 添加新角色
            userDTO.getRoles().forEach(roleName -> {
                roleRepository.findByName(roleName).ifPresent(user.getRoles()::add);
            });
        }

        return userRepository.save(user);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User addRoleToUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        
        if (!user.getRoles().contains(role)) {
            user.getRoles().add(role);
            userRepository.save(user);
        }
        
        return user;
    }

    @Override
    public User removeRoleFromUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        
        user.getRoles().remove(role);
        userRepository.save(user);
        
        return user;
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户名或密码错误"));

        // 确保在当前事务中加载角色
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            for (Role role : user.getRoles()) {
                authorities.add(new SimpleGrantedAuthority(role.getName()));
            }
        }
        
        // 如果用户有admin角色但authorities为空，手动添加
        if (authorities.isEmpty()) {
            Optional<Role> adminRole = roleRepository.findByName(Role.ROLE_ADMIN);
            if (adminRole.isPresent() && username.equals("admin")) {
                authorities.add(new SimpleGrantedAuthority(Role.ROLE_ADMIN));
                
                // 更新用户角色
                if (!user.getRoles().contains(adminRole.get())) {
                    user.getRoles().add(adminRole.get());
                    userRepository.save(user);
                }
            }
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getEnabled(),
                true,  // 账号不过期
                true,  // 凭证不过期
                true,  // 账号不锁定
                authorities
        );
    }
} 