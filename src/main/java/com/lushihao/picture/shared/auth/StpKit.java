package com.lushihao.picture.shared.auth;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

/**
 * StpLogic门面类 管理项目中的所有的StpLogic账号体系
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-06   19:33
 */
@Component
public class StpKit {
    public static final String SPACE_TYPE = "space";

    /**
     * 默认原生会话对象，项目中目前没有用到
     */
    public static final StpLogic DEFAULT = StpUtil.stpLogic;

    /**
     * Space会话对象 管理Space表的所有账户的登录故、权限认证
     */
    public static final StpLogic SPACE = new StpLogic(SPACE_TYPE);
}
