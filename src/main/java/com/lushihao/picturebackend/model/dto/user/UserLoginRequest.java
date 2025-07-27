package com.lushihao.picturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求列
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-27   17:00
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = -8107532784970522998L;
    // 账号
    private String userAccount;
    // 密码
    private String userPassword;

}


