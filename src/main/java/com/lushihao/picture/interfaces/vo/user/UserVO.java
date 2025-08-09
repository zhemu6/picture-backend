package com.lushihao.picture.interfaces.vo.user;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-27   20:12
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 8588839125881538585L;

    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;


    /**
     * 创建时间
     */
    private Date createTime;



}
