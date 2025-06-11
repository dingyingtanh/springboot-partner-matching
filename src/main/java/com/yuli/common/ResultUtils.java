package com.yuli.common;

/**
 * 返回工具类
 * @author yuli
 */
public class ResultUtils {

    /**
     * 成功
     * @param data 数据
     * @param <T> 泛型
     * @return BaseResponse
     */
    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(0,data,"ok");
    }

    /**
     * 失败
     * @param errorCode 错误码
     * @return BaseResponse
     */
    public static <T>  BaseResponse<T> error(ErrorCode errorCode){
        return new BaseResponse<T>(errorCode);
    }
    /**
     * 失败
     * @param errorCode 错误码
     * @return BaseResponse
     */
    public static  BaseResponse error(ErrorCode errorCode,String message,String description){
        return new BaseResponse<>(errorCode.getCode(),null, message, description);
    }

    /**
     * 失败
     * @param errorCode 错误码
     * @return BaseResponse
     */
    public static BaseResponse error(ErrorCode errorCode,String description){
        return new BaseResponse<>(errorCode.getCode(),null, errorCode.getMessage(),description);
    }

    /**
     * 错误
     * @param code 错误码
     * @param message 错误信息
     * @param description 错误描述
     * @return BaseResponse
     */
    public static BaseResponse error(int code,String message,String description){
        return new BaseResponse<>(code,null, message,description);
    }

}
