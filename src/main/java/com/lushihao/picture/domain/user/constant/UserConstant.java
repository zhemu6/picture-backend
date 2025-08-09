package com.lushihao.picture.domain.user.constant;

/**
 * 用户常量接口类
 * @author lushihao
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";
    
    // endregion

    /**
     * 用户默认头像
     */
    String DEFAULT_AVATAR = "https://picture-1327171626.cos.ap-beijing.myqcloud.com/avatar/nan_avatar.png";
}
