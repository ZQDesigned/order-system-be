package com.yiyunnetwork.order.config;

import com.yiyunnetwork.order.model.Role;
import com.yiyunnetwork.order.model.User;
import com.yiyunnetwork.order.repository.RoleRepository;
import com.yiyunnetwork.order.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/**
 * 初始化基础数据
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("初始化基础数据...");
        createRoles();
        createAdminUser();
        log.info("基础数据初始化完成");
    }

    private void createRoles() {
        // 如果角色已存在，则不创建
        if (roleRepository.findByName(Role.ROLE_ADMIN).isEmpty()) {
            Role adminRole = Role.builder()
                    .name(Role.ROLE_ADMIN)
                    .description("系统超管")
                    .build();
            roleRepository.save(adminRole);
            log.info("创建超管角色: {}", adminRole.getName());
        }

        if (roleRepository.findByName(Role.ROLE_AGENT).isEmpty()) {
            Role agentRole = Role.builder()
                    .name(Role.ROLE_AGENT)
                    .description("代理商")
                    .build();
            roleRepository.save(agentRole);
            log.info("创建代理角色: {}", agentRole.getName());
        }
    }

    private void createAdminUser() {
        // 如果超管用户已存在，则不创建
        if (userRepository.findByUsername("admin").isEmpty()) {
            Optional<Role> adminRole = roleRepository.findByName(Role.ROLE_ADMIN);
            if (adminRole.isPresent()) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .email("admin@yiyunnetwork.com")
                        .realName("系统管理员")
                        .enabled(true)
                        .roles(new ArrayList<>(Arrays.asList(adminRole.get())))
                        .build();
                userRepository.save(admin);
                log.info("创建超管用户: {}", admin.getUsername());
            }
        } else {
            // 如果用户已存在，则确保超管角色关联
            User admin = userRepository.findByUsername("admin").get();
            Optional<Role> adminRole = roleRepository.findByName(Role.ROLE_ADMIN);
            
            if (adminRole.isPresent() && admin.getRoles().isEmpty()) {
                admin.getRoles().add(adminRole.get());
                userRepository.save(admin);
                log.info("更新超管用户角色: {}", admin.getUsername());
            }
        }
    }
} 