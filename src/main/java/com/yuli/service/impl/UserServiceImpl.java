package com.yuli.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yuli.common.ErrorCode;
import com.yuli.common.ResultUtils;
import com.yuli.exception.BusinessException;
import com.yuli.model.User;
import com.yuli.service.UserService;
import com.yuli.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.yuli.contant.UserConstant.ADMIN_ROLE;
import static com.yuli.contant.UserConstant.USER_LOGIN_STATE;


/**
* @author yuli
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-05-28 17:12:10
*/
@Service
@Slf4j

public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 加盐 混淆密码
     */
    private static final String SALT = "yuli";


    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 密码校验
     * @param planetCode 星球编号
     * @return 新用户id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword,String planetCode) {
        //1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,  "用户密码过短");
        }
        // 账号不能包含特殊字符（只允许字母、数字和下划线）
        if (!userAccount.matches("\\w{4,}")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号包含特殊字符");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        if (planetCode.length() > 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,  "星球编号过长");
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long userCount = userMapper.selectCount(queryWrapper);
        if (userCount > 0) {
            throw new BusinessException(ErrorCode.NULL_ERROR,  "账号重复");
        }
        //星球编号不能重复
        queryWrapper.eq("planetCode", planetCode);
        long planetCount = userMapper.selectCount(queryWrapper);
        if (planetCount > 0) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"星球编号重复");
        }
        //  2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean save = this.save(user);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @return 脱敏后的用户信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 账号不能包含特殊字符（只允许字母、数字和下划线）
        if (!userAccount.matches("\\w{4,}")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号包含特殊字符");
        }
        //  2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        //用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        //用户脱敏
        User safeUser = getSafetyUser(user);
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safeUser);
        //4. 返回脱敏后的用户信息
        return safeUser;
    }

    /**
     * 用户脱敏
     * @param originUser 原始用户
     * @return 脱敏后的用户
     */
    @Override
         public User getSafetyUser(@NotNull User originUser) {
         User safeUser = new User();
         safeUser.setId(originUser.getId());
         safeUser.setUsername(originUser.getUsername());
         safeUser.setUserAccount(originUser.getUserAccount());
         safeUser.setAvatarUrl(originUser.getAvatarUrl());
         safeUser.setUserRole(originUser.getUserRole());
         safeUser.setGender(originUser.getGender());
         safeUser.setPhone(originUser.getPhone());
         safeUser.setEmail(originUser.getEmail());
         safeUser.setUserStatus(originUser.getUserStatus());
         safeUser.setCreateTime(originUser.getCreateTime());
         safeUser.setPlanetCode(originUser.getPlanetCode());
         safeUser.setTags(originUser.getTags());
         safeUser.setProFile(originUser.getProFile());
         return safeUser;
    }

    /**
     * 用户注销
     * @param request 请求
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);

        return 1;
    }

    /**
     * 根据标签搜索用户 (内存过滤   )
     * @param tagNameList 用户要拥有的标签
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //内存查询
       // 创建一个QueryWrapper对象，用于封装查询条件
       QueryWrapper<User> queryWrapper = new QueryWrapper<>();
       // 使用userMapper根据查询条件获取用户列表
       List<User> userList = userMapper.selectList(queryWrapper);
       // 创建一个Gson对象，用于JSON序列化和反序列化
       Gson  gson = new Gson();

       // 过滤用户列表，确保每个用户的标签集合中包含所有指定的标签
       return userList.stream().filter(user -> {
           // 获取用户的标签字符串
           String tagsStr = user.getTags();
           // 将用户的标签字符串反序列化为标签集合
           Set<String> tempTagNameSet =gson.fromJson(tagsStr, new TypeToken<Set<String>>() {}.getType());
           tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
           // 遍历指定的标签列表
           for (String tagName : tagNameList) {
               // 如果用户的标签集合中不包含当前标签，则返回false，表示该用户不符合条件
               if (!tempTagNameSet.contains(tagName)) {
                   return false;
               }
           }
           // 如果所有标签都匹配，则返回true，表示该用户符合条件
           return true;
       }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 更新用户信息
     * @param user 用户信息
     */
    @Override
    public int updateUser(User user, User loginUser) {
        long userId = user.getId();
        if (userId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 如果是管理员
        // 如果不是管理员，则只能更新当前用户
        if (!isAdmin(loginUser) && userId != loginUser.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    /**
     *  是否为管理员
     * @param request 请求
     * @return 是否为管理员
     */
    @Override
    public boolean isAdmin(HttpServletRequest request){
        // 仅管理员可查询
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user =  (User) attribute;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }
    /**
     *  是否为管理员
     * @param loginUser 请求
     * @return 是否为管理员
     */
    @Override
    public boolean isAdmin(User loginUser){
        // 仅管理员可查询
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 获取当前用户登录信息
     * @param request 请求
     * @return 当前用户登录信息
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return (User)userObj;
    }
//    /**
//     * 根据标签搜索用户(SQL 查询)
//     * @param tagNameList 用户要拥有的标签
//     */
//    @Deprecated
//    private List<User> searchUsersByTagsBySQL(List<String> tagNameList){
//        if (CollectionUtils.isEmpty(tagNameList)){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
////        sql查询
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        //like '%java$' and like '%python%'
//        for (String tagName : tagNameList) {
//            queryWrapper = queryWrapper.like("tags", tagName);
//        }
//        List<User> userList = userMapper.selectList(queryWrapper);
//        //将userList遍历后返回的参数进行脱敏  返回脱敏后的用户列表
//        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
//    }



}