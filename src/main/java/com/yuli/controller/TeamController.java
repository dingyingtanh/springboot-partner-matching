package com.yuli.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuli.common.BaseResponse;
import com.yuli.common.DeleteRequest;
import com.yuli.common.ErrorCode;
import com.yuli.common.ResultUtils;
import com.yuli.exception.BusinessException;
import com.yuli.model.domain.Team;
import com.yuli.model.domain.User;
import com.yuli.model.domain.UserTeam;
import com.yuli.model.dto.TeamQuery;
import com.yuli.model.request.TeamAddRequest;
import com.yuli.model.request.TeamJoinRequest;
import com.yuli.model.request.TeamQuitRequest;
import com.yuli.model.request.TeamUpdateRequest;
import com.yuli.model.vo.TeamUserVo;
import com.yuli.service.TeamService;
import com.yuli.service.UserService;
import com.yuli.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.record.DVALRecord;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yuli
 */
@Slf4j
@RestController
@RequestMapping("/team")
// 用户相关接口跨域
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class TeamController {

    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;
    @Resource
    private UserTeamService userTeamService;

    /**
     * 创建队伍
     * @param teamAddRequest 队伍
     * @param request 请求
     * @return 队伍id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        if (teamAddRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        User loginUser = userService.getLoginUser(request);
        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }
    /**
     * 删除队伍
     * @param deleteRequest 删除请求
     * @param request 请求
     * @return 删除结果
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){
        if (deleteRequest == null || deleteRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result =  teamService.deleteTeam(deleteRequest.getId(),loginUser);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }
    /**
     * 更新队伍
     * @param teamUpdateRequest 队伍
     * @param request 请求
     * @return 更新结果
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request){
        if (teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest,loginUser);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        return ResultUtils.success(true);
    }
    /**
     * 获取队伍
     * @param id 队伍id
     * @param request 请求
     * @return 队伍
     */
    @GetMapping("/get")
    public BaseResponse<Team> getByIdTeam(long id, HttpServletRequest request){
        if (id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(team);
    }
    /**
     * 获取队伍列表
     * @param teamQuery 队伍查询
     * @param request 请求
     * @return 队伍列表
     */
    //6.关联查询已加入队伍的用户信息（可能会很耗费性能，建议大家用自己写 SQL 的方式实现）todo
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVo>> listTeam(TeamQuery teamQuery ,HttpServletRequest request){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        // 1查询队伍列表
        List<TeamUserVo> teamList  = teamService.listTeams(teamQuery,isAdmin);
        final List<Long> teamIdList = teamList.stream().map(TeamUserVo::getId).collect(Collectors.toList());
        //2.判断当前用户是否已加入的队伍
        QueryWrapper<UserTeam> queryWrapperUserTeam = new QueryWrapper<>();
        try {
            User loginUser = userService.getLoginUser(request);
            queryWrapperUserTeam.eq("userId", loginUser.getId());
            queryWrapperUserTeam.in("teamId", teamIdList);
            List<UserTeam> userTeamsList = userTeamService.list(queryWrapperUserTeam);
            //已加入的队伍id集合
            Set<Long> hasJoinTeamIdSet = userTeamsList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //3.查询已加入的队伍
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        //队伍 id=> 加入这个队伍的
        Map<Long,List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size()));
        return ResultUtils.success(teamList);

    }
    /**
     * 获取分页的队伍
     * @param teamQuery 队伍查询
     * @param request 请求
     * @return 队伍列表
     */
    //TODO 优化分页查询
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listPageTeam(TeamQuery teamQuery,HttpServletRequest request){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        // 复制 teamQuery 到 team
        BeanUtils.copyProperties(teamQuery, team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> pageTeam = teamService.page(page, queryWrapper);
        if (pageTeam == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(pageTeam);
    }

    /**
     * 加入队伍
     * @param teamJoinRequest 队伍加入
     * @param request 请求
     * @return 加入结果
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request){
        if (teamJoinRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest,loginUser);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "加入失败");
        }
        return ResultUtils.success(result);
    }
    /**
     * 退出队伍
     * @param teamQuitRequest 队伍退出
     * @param request 请求
     * @return 退出结果
     */
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request){
        if (teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest,loginUser);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退出失败");
        }
        return ResultUtils.success(result);
    }
    /**
     * 获取当前用户创建的队伍列表
     * @param teamQuery 队伍查询
     * @param request 请求
     * @return 队伍列表
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVo>> listMyCreateTeams(TeamQuery teamQuery ,HttpServletRequest request){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        User loginUser = userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVo> listTeam = teamService.listTeams(teamQuery,isAdmin);
        return ResultUtils.success(listTeam);
    }
    /**
     * 获取当前用户加入的队伍列表
     * @param teamQuery 队伍查询
     * @param request 请求
     * @return 队伍列表
     */
    @GetMapping("list/my/join")
    public BaseResponse<List<TeamUserVo>> listMyTeams(TeamQuery teamQuery ,HttpServletRequest request){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",loginUser.getId());
        List<UserTeam> userTeamlist = userTeamService.list(queryWrapper);

        Map<Long, List<UserTeam>> listMap = userTeamlist.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        ArrayList<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        List<TeamUserVo> teamUserVos = teamService.listTeams(teamQuery, true);
        return ResultUtils.success(teamUserVos);
    }



}
