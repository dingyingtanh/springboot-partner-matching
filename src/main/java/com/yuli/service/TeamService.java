package com.yuli.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuli.model.domain.Team;
import com.yuli.model.domain.User;
import com.yuli.model.dto.TeamQuery;
import com.yuli.model.request.TeamJoinRequest;
import com.yuli.model.request.TeamUpdateRequest;
import com.yuli.model.vo.TeamUserVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
* @author dingy
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2026-01-07 23:12:10
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     * @param team 队伍
     * @param loginUser 登录用户
     * @return 队伍id
     */
    long addTeam(Team team, User loginUser);

    /**
     * 获取队伍列表
     * @param teamQuery 队伍查询
     * @param isAdmin 是否管理员
     * @return 队伍列表
     */
    List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin);

    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);
}
