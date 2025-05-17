package com.yiyunnetwork.order.service.impl;

import com.yiyunnetwork.order.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${system.display-name:订单系统}")
    private String systemName;

    @Override
    @Async("taskExecutor")
    public void sendVerificationCode(String to, String code) {
        String subject = "[" + systemName + "] 验证码";
        String content = "您好，\n\n"
                + "您的验证码是：" + code + "，有效期为10分钟。\n\n"
                + "如果这不是您的操作，请忽略此邮件。\n\n"
                + "此致，\n"
                + systemName + " 团队";
        
        sendSimpleTextEmail(to, subject, content);
    }

    @Override
    @Async("taskExecutor")
    public void sendOrderCreationNotification(String to, String orderNo, String orderLink) {
        String subject = "[" + systemName + "] 订单创建成功";
        
        String content = "您好，\n\n"
                + "您的订单已成功创建，订单号：" + orderNo + "。\n\n"
                + "您可以通过以下链接查看订单详情：\n"
                + orderLink + "\n\n"
                + "此致，\n"
                + systemName + " 团队";
        
        sendSimpleTextEmail(to, subject, content);
    }

    @Override
    @Async("taskExecutor")
    public void sendOrderStatusChangeNotification(String to, String orderNo, String status, 
                                                 String remarks, String orderLink) {
        String subject = "[" + systemName + "] 订单状态更新";
        
        String statusDesc = getStatusDescription(status);
        
        String content = "您好，\n\n"
                + "您的订单（订单号：" + orderNo + "）状态已更新为：" + statusDesc + "。\n\n";
        
        if (remarks != null && !remarks.isEmpty()) {
            content += "备注：" + remarks + "\n\n";
        }
        
        content += "您可以通过以下链接查看订单详情：\n"
                + orderLink + "\n\n"
                + "此致，\n"
                + systemName + " 团队";
        
        sendSimpleTextEmail(to, subject, content);
    }
    
    private String getStatusDescription(String status) {
        return switch (status) {
            case "PENDING_AGENT_REVIEW" -> "待代理审核";
            case "PENDING_ADMIN_REVIEW" -> "待超管审核";
            case "IN_PROGRESS" -> "订单进行中";
            case "COMPLETED" -> "已完成";
            case "REJECTED" -> "已退回";
            case "CANCELLED" -> "已取消";
            default -> status;
        };
    }

    @Override
    @Async("taskExecutor")
    public void sendSimpleTextEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            log.info("邮件已发送至 {}", to);
        } catch (Exception e) {
            log.error("邮件发送失败", e);
        }
    }
} 