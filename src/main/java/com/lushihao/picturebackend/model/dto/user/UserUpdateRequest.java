package com.lushihao.picturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新请求包装类
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-27   20:09
 */
@Data
public class UserUpdateRequest implements Serializable{

    private static final long serialVersionUID = -6833664993158209119L;

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

}
