package com.yuli.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍加入请求
 */
@Data
public class TeamJoinRequest implements Serializable {
    private static final long serialVersionUID = 8412981010694517942L;
    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;
}
