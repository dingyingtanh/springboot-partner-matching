package com.yuli.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuli.common.ErrorCode;
import com.yuli.common.TeamStatusCommon;
import com.yuli.exception.BusinessException;
import com.yuli.mapper.TeamMapper;
import com.yuli.model.domain.Team;
import com.yuli.model.domain.User;
import com.yuli.model.domain.UserTeam;
import com.yuli.model.dto.TeamQuery;
import com.yuli.model.request.TeamJoinRequest;
import com.yuli.model.request.TeamUpdateRequest;
import com.yuli.model.vo.TeamUserVo;
import com.yuli.model.vo.UserVo;
import com.yuli.service.TeamService;
import com.yuli.service.UserService;
import com.yuli.service.UserTeamService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


import java.util.*;

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

    @Override
    public List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if (teamQuery != null){
            //队伍id
            Long id = teamQuery.getId();
            if (id != null && id > 0){
                queryWrapper.eq("id", id);
            }
            //搜索关键词(同时搜索队伍名称和描述)
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)){
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            //队伍名称
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            //队伍描述
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)){
                queryWrapper.like("description", description);
            }
            //队伍最大人数相等的
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0){
                queryWrapper.eq("maxNum", maxNum);
            }
            //队伍创建人
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0){
                queryWrapper.eq("userId", userId);
            }
            //队伍状态
            Integer status = teamQuery.getStatus();
            TeamStatusCommon enumByValue = TeamStatusCommon.getEnumByValue(status);
            if (enumByValue == null){
                enumByValue = TeamStatusCommon.PUBLIC;
            }
            if (!isAdmin && !enumByValue.equals(TeamStatusCommon.PUBLIC)){
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq("status", enumByValue);
        }
        //不展示已过期的队伍
        //expireTime is null or expireTime > now()
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)){
            return new ArrayList<>();
        }
        List<TeamUserVo> teamUserVoList = new ArrayList<>();
        // 1. 自己写 SQL
        // 查询队伍和创建人的信息
        // select * from team t left join user u on t.userId = u.id
        // 查询队伍和已加入队伍成员的信息
        // select *
        // from team t
        // left join user_team ut on t.id = ut.teamId
        // left join user u on ut.userId = u.id;
        //2.关联查询创建人的用户信息
        for (Team team : teamList){
            Long userId = team.getUserId();
            if (userId == null){
                continue;
            }
            User user = userService.getById(userId);
            //用户脱敏
            User safetyUser = userService.getSafetyUser(user);
            TeamUserVo teamUserVo = new TeamUserVo();
            BeanUtils.copyProperties(team, teamUserVo);
            if (safetyUser != null){
                UserVo userVo = new UserVo();
                BeanUtils.copyProperties(safetyUser, userVo);
                teamUserVo.setCreateUser(userVo);
            }
            teamUserVoList.add(teamUserVo);
        }
        return teamUserVoList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest == null){
            return false;
        }
        Long id = teamUpdateRequest.getId();
        if (id == null || id <= 0){
            return false;
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //1.判断是否有权限修改
        //!Objects.equals(oldTeam.getUserId(), loginUser.getId()) 与这个相同 oldTeam.getUserId() != loginUser.getId()
        if (!Objects.equals(oldTeam.getUserId(), loginUser.getId()) && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        TeamStatusCommon enumByValue = TeamStatusCommon.getEnumByValue(teamUpdateRequest.getStatus());
        TeamStatusCommon oldTeamStatus = TeamStatusCommon.getEnumByValue(oldTeam.getStatus());
        // 如果从非加密状态转为加密状态，需要设置密码
        if (!oldTeamStatus.equals(TeamStatusCommon.SECRET) && enumByValue.equals(TeamStatusCommon.SECRET)) {
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须设置密码");
            }
        }
        // 如果原来是加密状态，现在改为其他状态，密码可以为空
        if (oldTeamStatus.equals(TeamStatusCommon.SECRET) && !enumByValue.equals(TeamStatusCommon.SECRET)) {
             teamUpdateRequest.setPassword(null);
        }
        // 校验队伍信息
        String name = teamUpdateRequest.getName();
        if (StringUtils.isNotBlank(name) && name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名称长度不能超过20");
        }
        
        String description = teamUpdateRequest.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述长度不能超过512");
        }
        
        // 校验过期时间
        Date expireTime = teamUpdateRequest.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "过期时间不能早于当前时间");
        }
        
        Team team = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, team);

        return this.updateById(team);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Long teamId = teamJoinRequest.getTeamId();
        if (teamId == null || teamId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        Integer status = team.getStatus();
        TeamStatusCommon teamStatus = TeamStatusCommon.getEnumByValue(status);
        if (TeamStatusCommon.PRIVATE.equals(teamStatus)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "禁止加入私有队伍");
        }
        String password = teamJoinRequest.getPassword();
        if (TeamStatusCommon.SECRET.equals(teamStatus)){
            if ( StringUtils.isBlank(password) || !password.equals(team.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        // 登录用户加入的队伍数量不能超过5个
        Long userId = loginUser.getId();
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasJoinNum = userTeamService.count(queryWrapper);
        if (hasJoinNum > 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建和加入5个队伍");
        }
        // 不能加入自己的队伍
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(" ", teamId);
        queryWrapper.eq("userId", userId);
        long hasUserJoinTeam = userTeamService.count(queryWrapper);
        if (hasUserJoinTeam > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");
        }
        // 队伍已满
        Integer maxNum = team.getMaxNum();
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        long teamHasJoinNum = userTeamService.count(queryWrapper);
        if (teamHasJoinNum >= maxNum){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
        }
        //新增队伍关联信息
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        return userTeamService.save(userTeam);
    }
}




