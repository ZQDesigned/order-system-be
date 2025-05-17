package com.yiyunnetwork.order.dto.user;

import com.yiyunnetwork.order.model.User;
import lombok.Builder;
import lombok.Data;

/**
 * 简化版用户DTO，只包含必要信息，不包含敏感字段
 */
@Data
@Builder
public class UserSimpleDTO {
    
    private Long id;
    private String username;
    private String realName;
    
    /**
     * 将User实体转换为UserSimpleDTO
     * 
     * @param user User实体
     * @return UserSimpleDTO
     */
    public static UserSimpleDTO fromUser(User user) {
        if (user == null) {
            return null;
        }
        
        return UserSimpleDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .build();
    }
} 