package com.lushihao.picture.domain.user.entity;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.lushihao.picture.domain.user.valueobject.UserRoleEnum;
import com.lushihao.picture.infrastructure.exception.ErrorCode;
import com.lushihao.picture.infrastructure.exception.ThrowUtils;
import com.lushihao.picture.interfaces.dto.user.UserLoginRequest;
import com.lushihao.picture.interfaces.dto.user.UserRegisterRequest;
import lombok.Data;

/**
 * 用户表
 *
 * @author lushihao
 * @TableName user
 */
@TableName(value = "user")
@Data
public class User implements Serializable {


    private static final long serialVersionUID = 3241743080851591533L;
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

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

    /**
     * 是否删除 通过@TableLogic 来表示 是不是
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 校验用户注册信息
     * @param userRegisterRequest 用户注册请求
     */
    public static void validUserRegister( UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        // 1.参数校验
        // 1.1 所有参数不能为空
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword), ErrorCode.PARAMS_ERROR, "用户名或密码不能为空");
        // 1.2 账号需要满足规范
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
        // 1.3 两个密码相同
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次密码不相同");
        // 1.4 密码需要满足规范(此时两个密码已经相同 直接鉴定其中一个即可)
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码过短");
    }

    /**
     * 校验用户登录先
     * @param userLoginRequest 用户登录请求
     */
    public static void validUserLogin( UserLoginRequest userLoginRequest) {
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        // 1.用户登录 首先还是对账号密码进行校验
        // 1.1 首先不能为空
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount,userPassword),ErrorCode.PARAMS_ERROR,"用户名或密码不能为空");
        // 1.1 用户名大于4和密码都要大于8
        ThrowUtils.throwIf(userAccount.length()<4,ErrorCode.PARAMS_ERROR,"用户账号过短");
        ThrowUtils.throwIf(userPassword.length()<8,ErrorCode.PARAMS_ERROR,"用户密码过短");
    }

    /**
     * 判断用户是否是管理员
     * @return boolean 是否是管理员
     */
    public boolean isAdmin(){
        return UserRoleEnum.Admin.getValue().equals(this.getUserRole());
    }





}