package com.lushihao.picture.shared.auth;

import com.lushihao.picture.domain.picture.entity.Picture;
import com.lushihao.picture.domain.space.entity.Space;
import com.lushihao.picture.domain.space.entity.SpaceUser;
import lombok.Data;

/**
 * 用户在特定空间内的授权上下文，包括关联的图片、空间和用户信息
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-07   9:08
 */
@Data
public class SpaceUserAuthContext {
    /**
     * 临时参数  不同请求的id可能会不同
     */
    private Long id;

    /**
     * 图片id
     */
    private Long pictureId;

    /**
     * 空间id
     */
    private Long spaceId;


    /**
     * 空间用户 ID
     */
    private Long spaceUserId;

    /**
     * 图片信息
     */
    private Picture picture;

    /**
     * 空间信息
     */
    private Space space;

    /**
     * 空间用户信息
     */
    private SpaceUser spaceUser;

}
