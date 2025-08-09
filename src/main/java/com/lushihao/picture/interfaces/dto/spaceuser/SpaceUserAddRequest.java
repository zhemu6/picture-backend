package com.lushihao.picture.interfaces.dto.spaceuser;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建空间成员
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-06   14:32
 */
@Data
public class SpaceUserAddRequest implements Serializable {

    private static final long serialVersionUID = -7605457790540548547L;


    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

}
