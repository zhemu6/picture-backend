package com.lushihao.picturebackend.manager.auth.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-06   16:39
 */
@Data
public class SpaceUserRole implements Serializable {


    private static final long serialVersionUID = -3314582277712267399L;
    /**
     * 角色的的key
     */
    private String key;
    /**
     * 角色名称
     */
    private String name;
    /**
     * 角色的权限键列表
     */
    private List<String> permissions;
    /**
     * 角色描述
     */
    private String description;

}
