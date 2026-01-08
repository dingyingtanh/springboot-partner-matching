package com.yuli.common;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import lombok.Getter;
import org.apache.poi.ss.formula.functions.T;

/**
 * 队伍状态枚举
 */


public enum TeamStatusCommon {
    // 0 - 公开，1 - 私有，2 - 加密
    PUBLIC(0, "公开"),
    PRIVATE(1, "私有"),
    SECRET(2, "加密");

    /**
     * 状态值
     */
    private int value;

    /**
     * 状态名字
     */
    private String text;

    /**
     * 根据 value 获取枚举
     * @param value 值
     * 0 - 公开，1 - 私有，2 - 加密
     */
    public static TeamStatusCommon getEnumByValue(Integer value){
        if (value == null){
            return null;
        }
        TeamStatusCommon[] teamStatus = TeamStatusCommon.values();
        for (TeamStatusCommon teamStatusCommon : teamStatus){
            if (teamStatusCommon.value == value){
                return teamStatusCommon;
            }
        }
        return null;
    }

    TeamStatusCommon(int value, String text){
        this.value = value;
        this.text = text;
    }

    public int getValue() {
        return value;
    }
    public int setValue(int value) {
        return this.value = value;
    }
    public String getText() {
        return text;
    }
    public String setText(String text) {
        return this.text = text;
    }
}
