package com.lushihao.picture.domain.space.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lushihao.picture.domain.space.entity.SpaceUser;
import com.lushihao.picture.interfaces.dto.spaceuser.SpaceUserQueryRequest;

import java.util.List;

/**
 * @author lushihao
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service
 * @createDate 2025-08-06 14:29:25
 */
public interface SpaceUserDomainService  {

    boolean save(SpaceUser spaceUser);

    LambdaQueryWrapper<SpaceUser> getLambdaQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    SpaceUser getByUserIdAndSpaceId(Long userId, Long spaceId);

    boolean removeById(long spaceId);

    SpaceUser getById(long spaceId);

    SpaceUser getOne(LambdaQueryWrapper<SpaceUser> lambdaQueryWrapper);

    List<SpaceUser> list(LambdaQueryWrapper<SpaceUser> lambdaQueryWrapper);

    boolean updateById(SpaceUser spaceUser);

    SpaceUser getSpaceUserBySpaceIdAndUserId(Long spaceId, Long userId);
}
