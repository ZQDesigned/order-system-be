package com.yiyunnetwork.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("订单系统 API")
                        .description("支持动态表单的订单管理系统API文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("易云网络")
                                .url("https://yiyunnetwork.com")
                                .email("contact@yiyunnetwork.com"))
                        .license(new License().name("私有许可").url("https://yiyunnetwork.com/license")))
                // 添加安全方案定义
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT认证令牌")))
                // 添加全局安全要求
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
} 