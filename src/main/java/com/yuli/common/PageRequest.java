package com.yuli.common;

import lombok.Data;

import java.io.Serializable;
/**
 * 分页请求请求参数
 */
@Data
public class PageRequest implements Serializable {
    private static final long serialVersionUID = 5674128082291861508L;

    /*
     * 当前页码
     */
    private int pageSize = 10;

    /*
     * 当前是第几页
     */
    private int pageNum = 1;

}
