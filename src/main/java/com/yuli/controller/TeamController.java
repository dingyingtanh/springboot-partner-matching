package com.yuli.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuli.common.BaseResponse;
import com.yuli.common.ErrorCode;
import com.yuli.common.ResultUtils;
import com.yuli.exception.BusinessException;
import com.yuli.model.domain.Team;
import com.yuli.model.domain.User;
import com.yuli.model.dto.TeamQuery;
import com.yuli.model.request.TeamAddRequest;
import com.yuli.model.request.TeamJoinRequest;
import com.yuli.model.request.TeamUpdateRequest;
import com.yuli.model.vo.TeamUserVo;
import com.yuli.service.TeamService;
import com.yuli.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author yuli
 */
@Slf4j
@RestController
@RequestMapping("/team")
// 用户相关接口跨域
@CrossOrigin(origins = "http://localhost:5173")
public class TeamController {

    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;

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
     * @param id 队伍id
     * @param request 请求
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody long id, HttpServletRequest request){
        if (id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = teamService.removeById(id);
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
    public BaseResponse<Team> getByIdTeam(@RequestBody long id, HttpServletRequest request){
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
    public BaseResponse<List<TeamUserVo>> listTeam(@RequestBody TeamQuery teamQuery ,HttpServletRequest request){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVo> listTeam  = teamService.listTeams(teamQuery,isAdmin);
        if (listTeam == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(listTeam);
    }
    /**
     * 获取分页的队伍
     * @param teamQuery 队伍查询
     * @param request 请求
     * @return 队伍列表
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listPageTeam(@RequestBody TeamQuery teamQuery,HttpServletRequest request){
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

}
