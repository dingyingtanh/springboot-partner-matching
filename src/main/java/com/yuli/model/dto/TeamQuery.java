package com.yuli.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.yuli.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 队伍查询封装类
 * 为什么要请求参数包装类？
 * 1. 为了封装查询条件，避免前端传递参数错误
 * 2. 为了避免前端传递参数过多，导致接口无法维护
 * 3. 为了避免前端传递参数错误，导致查询结果错误
 * 4. 为了避免前端传递参数过多，导致接口无法扩展
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeamQuery extends PageRequest {
    private static final long serialVersionUID = 7171935831054042712L;
    /**
     * id
     */
    private Long id;

    /**
     * 搜索关键词（同时队伍名称和描述）
     */
    private String searchText;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;


}
