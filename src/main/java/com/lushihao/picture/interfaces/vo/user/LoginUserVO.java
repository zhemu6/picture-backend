package com.lushihao.picture.interfaces.vo.user;

import lombok.Data;

import java.util.Date;

/**
 * 后端返回给前端的 脱敏
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-27   18:00
 */
@Data
public class LoginUserVO {

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
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}
