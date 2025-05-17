package com.yiyunnetwork.order.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    @JsonIgnore // 防止密码在JSON序列化时被包含
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column
    private String realName;

    @Column
    private String phone;

    @Column
    private Boolean enabled = true;

    @Column
    private LocalDateTime createTime;

    @Column
    private LocalDateTime lastLoginTime;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
} 