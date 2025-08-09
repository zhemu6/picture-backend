package com.lushihao.picture.interfaces.assemble;

import cn.hutool.core.bean.BeanUtil;
import com.lushihao.picture.domain.space.entity.SpaceUser;
import com.lushihao.picture.interfaces.dto.spaceuser.SpaceUserAddRequest;
import com.lushihao.picture.interfaces.dto.spaceuser.SpaceUserEditRequest;

/**
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-08   21:16
 */
public class SpaceUserAssembler {

    /**
     * 空间用户添加请求转换成实体类
     *
     * @param spaceUserAddRequest 空间用户添加请求
     * @return 空间用户实体类
     */
    public static SpaceUser toSpaceUserEntity(SpaceUserAddRequest spaceUserAddRequest) {
        SpaceUser spaceUser = new SpaceUser();
        BeanUtil.copyProperties(spaceUserAddRequest, spaceUser);
        return spaceUser;
    }

    /**
     * 空间用户更新请求转换成实体类
     *
     * @param spaceUserEditRequest 空间用户更新请求
     * @return 空间用户实体类
     */
    public static SpaceUser toSpaceUserEntity(SpaceUserEditRequest spaceUserEditRequest) {
        SpaceUser spaceUser = new SpaceUser();
        BeanUtil.copyProperties(spaceUserEditRequest, spaceUser);
        return spaceUser;
    }
    
}
