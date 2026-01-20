package com.yuli.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuli.model.domain.Team;
import com.yuli.model.domain.User;
import com.yuli.model.dto.TeamQuery;
import com.yuli.model.request.TeamJoinRequest;
import com.yuli.model.request.TeamQuitRequest;
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

    /**
     * 更新队伍
     * @param teamUpdateRequest 队伍
     * @param loginUser 登录用户
     * @return 更新结果
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest 队伍加入
     * @param loginUser 登录用户
     * @return 加入结果
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest 队伍退出
     * @param loginUser 登录用户
     * @return 退出结果
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 删除队伍
     * @param id 队伍id
     * @return 删除结果
     */
    boolean deleteTeam(long id,User loginUser);
}
