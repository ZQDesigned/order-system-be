package com.yiyunnetwork.order.util;

import com.yiyunnetwork.order.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

/**
 * 订单工具类
 */
@Component
@Slf4j
public class OrderUtils {

    private static final Random RANDOM = new Random();
    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Value("${application.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * 生成订单号
     * 格式: 日期时间(14位) + 随机数(6位)
     */
    public String generateOrderNo() {
        return LocalDateTime.now().format(ORDER_NO_FORMATTER) + String.format("%06d", RANDOM.nextInt(1000000));
    }

    /**
     * 生成访问令牌
     */
    public String generateAccessToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成订单访问链接
     */
    public String generateOrderLink(Order order) {
        return String.format("%s/orders/%s?token=%s", frontendUrl, order.getId(), order.getAccessToken());
    }

    /**
     * 生成二维码内容（订单访问链接）
     */
    public String generateQrCodeContent(Order order) {
        return generateOrderLink(order);
    }
} 