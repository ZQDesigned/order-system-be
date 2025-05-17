package com.yiyunnetwork.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * 配置RedisTemplate
     * 使用StringRedisSerializer替换默认的JdkSerializationRedisSerializer作为Key的序列化方式
     * 使用Jackson2JsonRedisSerializer替换默认的JdkSerializationRedisSerializer作为Value的序列化方式
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        
        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        
        template.afterPropertiesSet();
        return template;
    }
    
    /**
     * 配置StringRedisTemplate，用于操作字符串类型的数据
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(factory);
        return template;
    }
} 