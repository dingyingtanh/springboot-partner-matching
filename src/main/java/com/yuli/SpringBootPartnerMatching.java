package com.yuli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @author yuli
 */
@SpringBootApplication
// 开启定时任务
@EnableScheduling
@EnableRedisHttpSession
public class SpringBootPartnerMatching {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootPartnerMatching.class, args);
    }

}
