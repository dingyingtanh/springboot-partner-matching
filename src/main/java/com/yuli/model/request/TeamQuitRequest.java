package com.yuli.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 退出队伍请求
 * @author yuli
 */
@Data
public class TeamQuitRequest implements Serializable {
    private static final long serialVersionUID = -882008109709828540L;

    /**
     * 队伍id
     */
    private Long teamId;

}
