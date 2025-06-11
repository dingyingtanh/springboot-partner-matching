package com.yuli.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuli.common.BaseResponse;
import com.yuli.common.ErrorCode;
import com.yuli.common.ResultUtils;
import com.yuli.exception.BusinessException;
import com.yuli.model.User;
import com.yuli.model.request.UserLoginRequest;
import com.yuli.model.request.UserRegisterRequest;
import com.yuli.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.yuli.contant.UserConstant.ADMIN_ROLE;
import static com.yuli.contant.UserConstant.USER_LOGIN_STATE;

/**
 * @author yuli
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    /**
     * 用户注册
     * @param userRegisterRequester 注册请求
     * @return 用户id
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequester){
        if (userRegisterRequester == null){
//            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequester.getUserAccount();
        String userPassword = userRegisterRequester.getUserPassword();
        String checkPassword = userRegisterRequester.getCheckPassword();
        String planetCode = userRegisterRequester.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword,planetCode)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);

        return ResultUtils.success(result);
    }

    /**
     *  登录接口
     * @param userLoginRequest 登录请求
     * @param request 请求
     * @return 用户
     */

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest,HttpServletRequest request){
        if (userLoginRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 登出接口
     * @param request 请求
     * @return 是否登出成功
     */

    @PostMapping("/logout")
    private BaseResponse<Integer> userLogout(HttpServletRequest request){
        if (request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     *  获取当前用户
     * @param request 请求
     * @return 当前用户
     */
    @GetMapping("current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long id = currentUser.getId();
        User user = userService.getById(id);
        //TODO 校验是禁用
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);

    }

    /**
     *  搜索用户
     * @param username 用户名
     * @return 用户列表
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUser(String username, HttpServletRequest request){

        if (!isAdmin(request)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNoneBlank(username)){
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> collect = userList.stream().map(
                user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(collect);
    }

    /**
     *  删除用户
     * @param id 用户id
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id,HttpServletRequest request){
        if (!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id<=0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean resultBoolean = userService.removeById(id);
        return ResultUtils.success(resultBoolean);
    }

    /**
     *  是否为管理员
     * @param request 请求
     * @return 是否为管理员
     */
    private boolean isAdmin(HttpServletRequest request){
        // 仅管理员可查询
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user =  (User) attribute;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

}
