package com.lushihao.picture.shared.auth.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-06   16:39
 */
@Data
public class SpaceUserPermission implements Serializable {

    private static final long serialVersionUID = -461527608868525920L;


    /**
     * 权限的key
     */
    private String key;
    /**
     * 权限名称
     */
    private String name;
    /**
     * 描述
     */
    private String description;
}
