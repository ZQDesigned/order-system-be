package com.yiyunnetwork.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置
 * 提供线程池用于处理异步任务
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 通用任务线程池
     */
    @Bean("taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(5);
        // 最大线程数
        executor.setMaxPoolSize(20);
        // 队列容量
        executor.setQueueCapacity(100);
        // 线程名前缀
        executor.setThreadNamePrefix("task-");
        // 拒绝策略：抛出异常
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间（默认为0，此时立即停止），并没等待xx秒后强制停止
        executor.setAwaitTerminationSeconds(60);
        // 初始化
        executor.initialize();
        return executor;
    }
} 