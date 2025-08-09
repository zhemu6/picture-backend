package com.lushihao.picture.interfaces.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-27   20:08
 */
@Data
public class UserAddRequest implements Serializable{

    private static final long serialVersionUID = 8277244950093393098L;
    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色: user, admin
     */
    private String userRole;


}
