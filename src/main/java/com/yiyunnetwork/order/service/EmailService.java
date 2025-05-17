package com.yiyunnetwork.order.service;

/**
 * 邮件服务接口
 */
public interface EmailService {
    
    /**
     * 发送验证码邮件
     * 
     * @param to   收件人
     * @param code 验证码
     */
    void sendVerificationCode(String to, String code);
    
    /**
     * 发送订单创建通知邮件
     * 
     * @param to         收件人
     * @param orderNo    订单编号
     * @param orderLink  订单访问链接
     */
    void sendOrderCreationNotification(String to, String orderNo, String orderLink);
    
    /**
     * 发送订单状态变更通知邮件
     * 
     * @param to        收件人
     * @param orderNo   订单编号
     * @param status    订单状态
     * @param remarks   备注
     * @param orderLink 订单访问链接
     */
    void sendOrderStatusChangeNotification(String to, String orderNo, String status, 
                                          String remarks, String orderLink);
    
    /**
     * 发送简单文本邮件
     * 
     * @param to      收件人
     * @param subject 主题
     * @param text    内容
     */
    void sendSimpleTextEmail(String to, String subject, String text);
} 