package com.yuli.model.request;


import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * @author yuli
 */
@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUID = -8541178845793228234L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;

    private String planetCode;
}
