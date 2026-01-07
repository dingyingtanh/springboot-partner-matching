package com.yuli.service;

import com.yuli.config.RedissonConfig;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.yuli.contant.RedisConstant.REDIS_KEY_DOCACHE_LOCK;
import static com.yuli.contant.RedisConstant.REDIS_KEY_YULI_PRECACHEJOB;

@SpringBootTest
public class RedissonTest {
    @Resource
    private RedissonClient redissonClient;

    @Test
    void redisTest(){

        //list 数据库在本地JVM内存中
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("yuli");
        System.out.println("打印list"+arrayList.get(0));

        arrayList.remove(0);

        RList<String> redisList = redissonClient.getList("test-yuli");
        redisList.add("yuli");
        System.out.println("打印redisList"+redisList.get(0));
        redisList.remove(0);

        Map<String, Integer> map = new HashMap<>();
        map.put("yuli",60);
        System.out.println("打印map"+map.get("yuli"));
        RMap<String, Integer> redisMap = redissonClient.getMap("test-yuli-map");
        redisMap.put("yuli",10);
        System.out.println("打印redisMap"+redisMap.get("yuli"));
        redisMap.remove("yuli");
    }
    @Test
    //看门狗 debug
    void testWatchDog(){
        RLock lock = redissonClient.getLock(REDIS_KEY_YULI_PRECACHEJOB + REDIS_KEY_DOCACHE_LOCK);
        try {
            if (lock.tryLock(0,-1, TimeUnit.MILLISECONDS)){
                Thread.sleep(5000); // 模拟业务逻辑执行时间
                System.out.println("Lock acquired, doing something..."+Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }finally {
            //判断是否是自己的锁在释放锁
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }
}
