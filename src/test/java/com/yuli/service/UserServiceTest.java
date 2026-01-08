package com.yuli.service;

import com.yuli.common.TeamStatusCommon;
import com.yuli.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class UserServiceTest {
    @Resource
    UserService userService;
    @Test
    public void test() {
        User user = new User();
        user.setUsername("ding");
        user.setUserAccount("122");
        user.setAvatarUrl("");
        user.setGender(0);
        user.setUserPassword("12345678");
        user.setPhone("123");
        user.setEmail("456");
        boolean result = userService.save(user);
        System.out.println(result);

    }
    @Test
    public void test1() {
        TeamStatusCommon teamStatus = TeamStatusCommon.getEnumByValue(1);
        System.out.println(teamStatus.getText());

    }
}
