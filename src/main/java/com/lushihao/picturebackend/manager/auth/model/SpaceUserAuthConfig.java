package com.lushihao.picturebackend.manager.auth.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-06   16:37
 */
@Data
public class SpaceUserAuthConfig implements Serializable {

    private static final long serialVersionUID = -6043031929212755681L;

    /**
     * 权限控制列表
     */
    private List<SpaceUserPermission> permissions;

    /**
     * 角色列表
     */
    private List<SpaceUserRole> roles;

}
