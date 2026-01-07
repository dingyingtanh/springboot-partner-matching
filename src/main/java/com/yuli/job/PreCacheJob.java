package com.yuli.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuli.model.User;
import com.yuli.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.yuli.contant.RedisConstant.*;


@Component
@Slf4j
public class    PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    //重点用户
    private List<Long> mainUserList = Arrays.asList(1L);

    //每天执行，预热推荐用户
    @Scheduled(cron = "0 12 1 * * *")
    public void doCacheRecommendUser(){
        //设置锁
        RLock lock = redissonClient.getLock(REDIS_KEY_YULI_PRECACHEJOB + REDIS_KEY_DOCACHE_LOCK);
        //尝试获取锁
        try {
            //尝试获取锁，最多等待0秒，这里是-1是看门狗机制 只有一个线程获取锁
            if (lock.tryLock(0,-1,TimeUnit.MILLISECONDS)){
                System.out.println("get lock"+Thread.currentThread().getId());
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                Page<User> page = userService.page(new Page<>(1, 20), queryWrapper);
                String redisKey = String.format("yuli:user:recommend", mainUserList);
                ValueOperations valueOperations = redisTemplate.opsForValue();
                //写缓存，30s过期
                try{
                    valueOperations.set(redisKey, page, 30000, TimeUnit.MILLISECONDS);
                }catch (Exception e){
                    log.error("redis set key error", e);
                }finally {
                    //判断是否是自己的锁在释放锁
                    if (lock.isHeldByCurrentThread()){
                        System.out.println("del lock"+Thread.currentThread().getId());
                        lock.unlock();
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}
