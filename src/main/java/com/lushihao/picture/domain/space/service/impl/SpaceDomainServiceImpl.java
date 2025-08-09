package com.lushihao.picture.domain.space.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.picture.domain.picture.entity.Picture;
import com.lushihao.picture.domain.space.entity.Space;
import com.lushihao.picture.domain.space.repository.SpaceRepository;
import com.lushihao.picture.domain.space.service.SpaceDomainService;
import com.lushihao.picture.domain.space.valueobject.SpaceLevelEnum;
import com.lushihao.picture.domain.space.valueobject.SpaceTypeEnum;
import com.lushihao.picture.infrastructure.exception.ErrorCode;
import com.lushihao.picture.infrastructure.exception.ThrowUtils;
import com.lushihao.picture.interfaces.dto.space.SpaceQueryRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lushihao
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-08-02 20:29:16
 */
@Service
public class SpaceDomainServiceImpl implements SpaceDomainService {


    @Resource
    private SpaceRepository spaceRepository;

    @Override
    public boolean removeById(Long spaceId) {
        return spaceRepository.removeById(spaceId);
    }


    @Override
    public Space getById(Long spaceId) {
        return spaceRepository.getById(spaceId);
    }

    /**
     * 根据查询条件古剑LambdaQueryWrapper对象
     *
     * @param spaceQueryRequest 查询条件
     * @return LambdaQueryWrapper对象
     */
    @Override
    public LambdaQueryWrapper<Space> getLambdaQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAMS_ERROR);

        Long id = spaceQueryRequest.getId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Long userId = spaceQueryRequest.getUserId();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();
        Integer spaceType = spaceQueryRequest.getSpaceType();

        LambdaQueryWrapper<Space> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(id), Space::getId, id);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(spaceType), Space::getSpaceType, spaceType);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(userId), Space::getUserId, userId);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), Space::getSpaceLevel, spaceLevel);
        lambdaQueryWrapper.like(StrUtil.isNotBlank(spaceName), Space::getSpaceName, spaceName);


        final Map<String, SFunction<Space, ?>> sortFieldMap;
        Map<String, SFunction<Space, ?>> map = new HashMap<>();
        map.put("id", Space::getId);
        map.put("userId", Space::getUserId);
        map.put("spaceName", Space::getSpaceName);
        map.put("getSpaceType", Space::getSpaceType);
        map.put("spaceLevel", Space::getSpaceLevel);


        sortFieldMap = Collections.unmodifiableMap(map);

        if (StrUtil.isNotEmpty(sortField)) {
            boolean isAsc = "ascend".equalsIgnoreCase(sortOrder);
            SFunction<Space, ?> sortFunc = sortFieldMap.get(sortField);
            if (sortFunc != null) {
                lambdaQueryWrapper.orderBy(true, isAsc, sortFunc);
            }
        }

        return lambdaQueryWrapper;
    }


    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum enumByValue = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (enumByValue != null) {
            // 如果管理员传入的max size 和maxCount 不为空 则使用管理员设置的值 否则采用原来的
            if (space.getMaxSize() == null) {
                space.setMaxSize(enumByValue.getMaxSize());
            }
            if (space.getMaxCount() == null) {
                space.setMaxCount(enumByValue.getMaxCount());
            }
        }
        ThrowUtils.throwIf(enumByValue == null, ErrorCode.PARAMS_ERROR, "空间等级不存在");
    }


    @Override
    public boolean save(Space space) {
        return spaceRepository.save(space);
    }


    @Override
    public boolean getSpaceByUserIdAndSpaceType(Long loginUserId, Integer spaceType) {
        return spaceRepository.lambdaQuery()
                .eq(Space::getUserId, loginUserId)
                .eq(Space::getSpaceType, spaceType)
                .exists();
    }

    @Override
    public boolean updateById(Space space) {
        return spaceRepository.updateById(space);
    }


    @Override
    public Page<Space> page(Page<Space> spacePage, LambdaQueryWrapper<Space> lambdaQueryWrapper) {
        return spaceRepository.page(spacePage, lambdaQueryWrapper);
    }

    @Override
    public List<Space> listByIds(Set<Long> spaceIdSet) {
        return spaceRepository.listByIds(spaceIdSet);
    }

    @Override
    public boolean addUpdatePicture(Long finalSpaceId, Long picSize) {
        return  spaceRepository.addUpdatePicture(finalSpaceId, picSize);
    }

    @Override
    public boolean deleteUpdatePicture(Long spaceId, Long picSize) {
        return spaceRepository.deleteUpdatePicture(spaceId, picSize);
    }

    @Override
    public List<Space> list(QueryWrapper<Space> queryWrapper) {
        return spaceRepository.list(queryWrapper);
    }

    @Override
    public Set<Long> getSpaceId(int value) {
        return spaceRepository.lambdaQuery()
                .eq(Space::getSpaceType, SpaceTypeEnum.TEAM.getValue())
                .list()
                .stream()
                .map(Space::getId)
                .collect(Collectors.toSet());
    }


}




