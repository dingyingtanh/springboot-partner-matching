package com.yuli.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuli.mapper.UserTeamMapper;
import com.yuli.model.domain.UserTeam;
import com.yuli.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author dingy
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2026-01-07 23:19:27
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




