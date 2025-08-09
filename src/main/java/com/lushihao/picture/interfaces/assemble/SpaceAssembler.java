package com.lushihao.picture.interfaces.assemble;

import cn.hutool.core.bean.BeanUtil;
import com.lushihao.picture.domain.space.entity.Space;
import com.lushihao.picture.interfaces.dto.space.SpaceAddRequest;
import com.lushihao.picture.interfaces.dto.space.SpaceEditRequest;
import com.lushihao.picture.interfaces.dto.space.SpaceUpdateRequest;

/**
 * 空间对象转换
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-08   21:39
 */
public class SpaceAssembler {

    /**
     * 空间添加请求转换成实体类
     *
     * @param spaceEditRequest 空间添加请求
     * @return 空间实体类
     */
    public static Space toSpaceEntity(SpaceEditRequest spaceEditRequest) {
        Space space = new Space();
        BeanUtil.copyProperties(spaceEditRequest, space);
        return space;
    }

    /**
     * 空间更新请求转换成实体类
     *
     * @param spaceAddRequest 空间更新请求
     * @return 空间实体类
     */
    public static Space toSpaceEntity(SpaceAddRequest spaceAddRequest) {
        Space space = new Space();
        BeanUtil.copyProperties(spaceAddRequest, space);
        return space;
    }

    /**
     * 空间更新请求转换成实体类
     *
     * @param spaceUpdateRequest 空间更新请求
     * @return 空间实体类
     */
    public static Space toSpaceEntity(SpaceUpdateRequest spaceUpdateRequest) {
        Space space = new Space();
        BeanUtil.copyProperties(spaceUpdateRequest, space);
        return space;
    }
}
