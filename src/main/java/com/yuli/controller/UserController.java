package com.yuli.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuli.common.BaseResponse;
import com.yuli.common.ErrorCode;
import com.yuli.common.ResultUtils;
import com.yuli.utils.PageVoUtils;
import com.yuli.exception.BusinessException;
import com.yuli.model.domain.User;
import com.yuli.model.request.UserLoginRequest;
import com.yuli.model.request.UserRegisterRequest;
import com.yuli.model.vo.PageVo;
import com.yuli.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.yuli.contant.UserConstant.USER_LOGIN_STATE;

/**
 * @author yuli
 */
@Slf4j
@RestController
@RequestMapping("/user")
// 用户相关接口跨域
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

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
        if (currentUser == null || currentUser.getId() <= 0){
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

        if (!userService.isAdmin( request)){
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
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        List<User> users = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(users);
    }

    /**
     *  删除用户
     * @param id 用户id
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id,HttpServletRequest request){
        if (!userService.isAdmin( request)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id<=0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean resultBoolean = userService.removeById(id);
        return ResultUtils.success(resultBoolean);
    }

    /**
     * 更新用户
     * @param user 用户
     * @return 是否更新成功
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user,HttpServletRequest request){
        //1.校验参数是否为空
        if (user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (user.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //TODO: 补充校验，如果用户没有传任何要更新的值，就直接报错，不用执行 update更新语句
        if (user.getUserPassword() != null && user.getUserPassword().length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }


        User loginUser =  userService.getLoginUser(request);
        //更新用户数据
        int result = userService.updateUser(user,loginUser);
        return ResultUtils.success(result);
    }
    @GetMapping("/recommend")
    public BaseResponse<PageVo> recommendUsers(long pageSize, long pageNum, HttpServletRequest request){
        User loginUser =  userService.getLoginUser(request);
        IPage<User> page = new Page<>(pageNum,pageSize);
        PageVoUtils pageVoUtils = new PageVoUtils();

        String redisKey = String.format("yuli:user:recommend", loginUser.getId());
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //有缓存直接读取
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if (userPage != null){
            return ResultUtils.success(pageVoUtils.pageVo(userPage));
        }
        //无缓存，查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum,pageSize),queryWrapper);
        try{
            valueOperations.set(redisKey,userPage,30000, TimeUnit.MILLISECONDS);
        } catch (Exception e){
            log.error("redis set key error",e);
        }
        return ResultUtils.success(pageVoUtils.pageVo(userPage));
    }
}
