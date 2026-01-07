package com.yuli.service;

import com.yuli.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dingy
 */
@SpringBootTest
public class InsertUserTest {
    @Resource
    private UserService userService;
    final int INSERT_NUM_1 = 100000;
    /**
     * 插入用户
     */
    // 定时任务
    @Test
    public void doInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        System.out.println("开始插入用户");
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM_1; i++) {
            User user = new User();
            user.setUsername("假用户");
            user.setUserAccount("郁离");
            user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/avatar/1596538401635.png");
            user.setGender(0);
            // 密码正常要加密一下
            user.setUserPassword("12345678");
            user.setPhone("12345678901");
            user.setEmail("123@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("1");
            user.setTags("[\"男\",\"java\",\"emo\",\"前端\",\"web\",\"vue\"]");
            userList.add(user);
        }
        userService.saveBatch(userList, 1000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
    /**
     * 并发插入用户
     */
    // 定时任务
    @Test
    public void doConcurrentInsertUser() {
        StopWatch stopWatch = new StopWatch();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        stopWatch.start();
        final int INSERT_NUM_1 = 100000;
        // 分十组
        int j = 0;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            List<User> userList = new ArrayList<>();
            while (true){
                j++;
                User user = new User();
                user.setUsername("假用户");
                user.setUserAccount("郁离");
                user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/avatar/1596538401635.png");
                user.setGender(0);
                // 密码正常要加密一下
                user.setUserPassword("12345678");
                user.setPhone("12345678901");
                user.setEmail("123@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setPlanetCode("1");
                user.setTags("[\"男\",\"java\",\"emo\",\"前端\",\"web\",\"vue\"]");
                userList.add(user);
                if (j % 1000 == 0){
                    break;
                }
            }
            // 异步批量插入
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                userService.saveBatch(userList, 1000);
            },executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
