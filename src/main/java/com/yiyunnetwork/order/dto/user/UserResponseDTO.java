package com.yiyunnetwork.order.dto.user;

import com.yiyunnetwork.order.model.Role;
import com.yiyunnetwork.order.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    
    private Long id;
    private String username;
    private String email;
    private String realName;
    private String phone;
    private Boolean enabled;
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;
    private List<String> roleNames;
    
    public static UserResponseDTO fromUser(User user) {
        List<String> roleNames = new ArrayList<>();
        if (user.getRoles() != null) {
            roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
        }
        
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .enabled(user.getEnabled())
                .createTime(user.getCreateTime())
                .lastLoginTime(user.getLastLoginTime())
                .roleNames(roleNames)
                .build();
    }
} 