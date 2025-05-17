package com.yiyunnetwork.order.dto.auth;

import com.yiyunnetwork.order.dto.user.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private UserResponseDTO user;
} 