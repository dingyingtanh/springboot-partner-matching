package com.yuli.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import com.yuli.common.ErrorCode;
import com.yuli.common.TeamStatusCommon;
import com.yuli.exception.BusinessException;
import com.yuli.mapper.TeamMapper;
import com.yuli.mapper.UserTeamMapper;
import com.yuli.model.domain.Team;
import com.yuli.model.domain.User;
import com.yuli.model.domain.UserTeam;
import com.yuli.service.TeamService;
import com.yuli.service.UserService;
import com.yuli.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


import java.util.Date;
import java.util.Optional;

/**
* @author dingy
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2026-01-07 23:12:10
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

    @Resource
    UserService userService;
    @Resource
    private TeamService teamService;
    @Resource
    private UserTeamService userTeamService;

    @Override
    // 添加队伍
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        // 1.请求参数不能为空
        if (team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2.用户必须登录
        if (loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //用户id
        final long userId = loginUser.getId();

        /*
         3.队伍人数 > 1 且 <= 20
         这段代码使用了Optional来处理team.getMaxNum()可能为null的情况，
         如果为null则默认为0。从上下文来看，这是在验证队伍最大人数是否在合理范围内（1-20之间）。
         */
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum <= 1 || maxNum > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不符合要求");
        }

        /*
         4.队伍标题<=20
         StringUtils.isBlank 判断字符串是否为空、空白字符串（仅含空格 / 制表符 / 换行符等）或 null。
         */
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不符合要求");
        }
        //5.描述<=512
        // StringUtils.isNotEmpty 函数判断字符串是否不为空且长度大于0。
        String description = team.getDescription();
        if (StringUtils.isNotEmpty(description) && description.length() > 512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }
        //6.是否公开(默认)0 - 公开，1 - 私有，2 - 加密
        Integer status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusCommon enumByValue = TeamStatusCommon.getEnumByValue(status);
        if (enumByValue == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态错误");
        }
        //6.队伍密码<=32
        String password = team.getPassword();
        if (TeamStatusCommon.SECRET.equals(enumByValue)){
            if (StringUtils.isBlank(password) || password.length() < 32){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }
        //7.超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        // 判断当前时间是否在超时时间之后
        if (new Date().after(expireTime)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间设置不正确");
        }
        //8.用户最多创建5个队伍
        //TODO 有 bug，可能同时创建 100 个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasTeamNum  = this.count(queryWrapper);
        if (hasTeamNum  >= 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建5个队伍");
        }
        //9. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean saveTeam = this.save(team);
        Long teamId = team.getId();
        if (!saveTeam){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        //插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(team.getUserId());
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        boolean saveUserTeam = userTeamService.save(userTeam);
        if (!saveUserTeam){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        return teamId;
    }
}




