package com.yuli.once;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 星球用户信息表
 */
@Data
public class XingQiuUserInfo {
    /**
     *  id
     */
    @ExcelProperty("星球编号")
    private String planetCode;

    /**
     *  用户呢称
     */
    @ExcelProperty("成员昵称")
    private String username;
}
