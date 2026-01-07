package com.yuli.config;


import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {
    @Value("${spring.redis.port}")
    private String port;
    @Value("${spring.redis.host}")
    private String host;
    @Bean
    public RedissonClient redissonClient() {
        //1.创建配置
        Config config = new Config();
        String.format("redis://%s:%s", host, port);
        // 使用单个redis,没有开集群 useSingleServer()设置地址和使用库
        config.useSingleServer().setAddress(String.format("redis://%s:%s", host, port)).setDatabase(3);
        //2.创建RedissonClient对象并返回
        return Redisson.create(config);
    }
}
