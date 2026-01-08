package com.yuli.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 标签
 * @author yuli
 * @TableName tag
 */
@TableName(value ="tag")
@Data
public class Tag {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 父标签 id
     */
    private Long parentId;

    /**
     * 0 - 不是, 1 - 父标签
     */
    private Integer isParent;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 是否删除
     * TableLogic
     * 当执行删除操作时，不会从数据库中物理删除记录，而是将 isDelete 字段设置为特定值（通常是1）
     * 当查询数据时，MyBatis-Plus 会自动添加条件过滤掉已标记为删除的记录（isDelete=0 的记录）
     */
    @TableLogic
    private Integer isDelete;
}