package com.lushihao.picturebackend.manager.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.lushihao.picturebackend.manager.auth.model.SpaceUserAuthConfig;
import com.lushihao.picturebackend.manager.auth.model.SpaceUserRole;
import com.lushihao.picturebackend.model.entity.Space;
import com.lushihao.picturebackend.model.entity.SpaceUser;
import com.lushihao.picturebackend.model.entity.User;
import com.lushihao.picturebackend.model.enums.SpaceRoleEnum;
import com.lushihao.picturebackend.model.enums.SpaceTypeEnum;
import com.lushihao.picturebackend.service.SpaceService;
import com.lushihao.picturebackend.service.SpaceUserService;
import com.lushihao.picturebackend.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 空间权限管理 用于加载配置文件到对象中
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-06   16:44
 */
@Component
public class SpaceUserAuthManager implements Serializable {

    private static final long serialVersionUID = 5445682473204341074L;

    public static final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG;

    @Resource
    private UserService userService;
    @Resource
    private SpaceUserService spaceUserService;

    static {
        String json = ResourceUtil.readUtf8Str("biz/spaceUserAuthConfig.json");
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(json, SpaceUserAuthConfig.class);
    }

    /**
     * 通过用户角色去查询用户的权限
     *
     * @param spaceUserRole 用户角色
     * @return 权限list
     */
    public List<String> getPermissionsByRole(String spaceUserRole) {
        if (StrUtil.isBlank(spaceUserRole)) {
            return new ArrayList<>();
        }
        SpaceUserRole role = SPACE_USER_AUTH_CONFIG.getRoles().stream()
                .filter(r -> spaceUserRole.equals(r.getKey()))
                .findFirst()
                .orElse(null);
        if (role == null) {
            return new ArrayList<>();
        }
        return role.getPermissions();
    }

    /**
     * 获取登录用户权限列表
     * @param space
     * @param loginUser
     * @return
     */
    public List<String> getPermissionList(Space space, User loginUser) {
        if (loginUser == null) {
            return new ArrayList<>();
        }
        // 管理员权限
        List<String> ADMIN_PERMISSIONS = getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        // 公共图库
        if (space == null) {
            if (userService.isAdmin(loginUser)) {
                return ADMIN_PERMISSIONS;
            }
            return new ArrayList<>();
        }
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(space.getSpaceType());
        if (spaceTypeEnum == null) {
            return new ArrayList<>();
        }
        // 根据空间获取对应的权限
        switch (spaceTypeEnum) {
            case PRIVATE:
                // 私有空间，仅本人或管理员有所有权限
                if (space.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {
                    return ADMIN_PERMISSIONS;
                } else {
                    return new ArrayList<>();
                }
            case TEAM:
                // 团队空间，查询 SpaceUser 并获取角色和权限
                SpaceUser spaceUser = spaceUserService.lambdaQuery()
                        .eq(SpaceUser::getSpaceId, space.getId())
                        .eq(SpaceUser::getUserId, loginUser.getId())
                        .one();
                if (spaceUser == null) {
                    return new ArrayList<>();
                } else {
                    return getPermissionsByRole(spaceUser.getSpaceRole());
                }
        }
        return new ArrayList<>();
    }

}
