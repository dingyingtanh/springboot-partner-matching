package com.yuli.once;

import com.yuli.mapper.UserMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author dingy
 */
@Component


public class InsertUser {
    @Resource
    private UserMapper userMapper;
    final int INSERT_NUM_1 = 1000;
    /**
     * 插入用户
     */
    // 定时任务
//    @Scheduled(fixedDelay = 5000)
//    public void doInsertUser() {
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//        System.out.println("开始插入用户");
//        for (int i = 0; i < INSERT_NUM_1; i++) {
//            User user = new User();
//            user.setUsername("假用户");
//            user.setUserAccount("郁离");
//            user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/avatar/1596538401635.png");
//            user.setGender(0);
//            // 密码正常要加密一下
//            user.setUserPassword("12345678");
//            user.setPhone("12345678901");
//            user.setEmail("123@qq.com");
//            user.setUserStatus(0);
//            user.setUserRole(0);
//            user.setPlanetCode("1");
//            user.setTags("[]");
//            userMapper.insert(user);
//        }
//        stopWatch.stop();
//        System.out.println(stopWatch.getTotalTimeMillis());
//    }
}
