package com.lushihao.picture.interfaces.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求列
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-27   17:00
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -8937081540232621000L;

    // 账号
    private String userAccount;
    // 密码
    private String userPassword;
    // 确认密码
    private String checkPassword;

}


