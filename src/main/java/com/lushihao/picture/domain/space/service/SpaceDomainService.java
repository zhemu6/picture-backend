package com.lushihao.picture.domain.space.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.picture.domain.picture.entity.Picture;
import com.lushihao.picture.domain.space.entity.Space;
import com.lushihao.picture.interfaces.dto.space.SpaceQueryRequest;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author lushihao
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-08-02 20:29:16
 */
public interface SpaceDomainService {


    boolean removeById(Long spaceId);

    Space getById(Long spaceId);

    /**
     * 根据查询条件古剑LambdaQueryWrapper对象
     *
     * @param spaceQueryRequest 查询条件
     * @return LambdaQueryWrapper对象
     */
    LambdaQueryWrapper<Space> getLambdaQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 传入一个空间对象 根据这个空间对象的level 自动分配max-size 和max-count
     *
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);


    boolean save(Space space);


    boolean getSpaceByUserIdAndSpaceType(Long loginUserId, Integer spaceType);

    boolean updateById(Space space);


    Page<Space> page(Page<Space> spacePage, LambdaQueryWrapper<Space> lambdaQueryWrapper);

    List<Space> listByIds(Set<Long> spaceIdSet);

    boolean addUpdatePicture(Long finalSpaceId, Long picSize);

    boolean deleteUpdatePicture(Long spaceId, Long picSize);

    List<Space> list(QueryWrapper<Space> queryWrapper);

    Set<Long> getSpaceId(int value);
}
