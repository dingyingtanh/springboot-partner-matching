package com.yuli.service;

import com.yuli.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {
    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void testRedis() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //增
        valueOperations.set("key1", "value1");
        valueOperations.set("key2", 1);
        valueOperations.set("shayuDouble",2.0);
        User user = new User();
        user.setId(1L);
        user.setUsername("shayu");
        valueOperations.set("yuliUser", user);

        //查
        Object yuli = valueOperations.get("key1");
        Assertions.assertTrue("value1".equals((String)yuli));
        yuli = valueOperations.get("key2");
        Assertions.assertTrue(1L == (Integer) yuli);
        yuli = valueOperations.get("shayuDouble");
        Assertions.assertTrue(2.0 == (Double) yuli);
        System.out.println(valueOperations.get("yuliUser"));
        valueOperations.set("yuliString","yuli");
        Boolean key2 = redisTemplate.delete("key2");
    }
}
