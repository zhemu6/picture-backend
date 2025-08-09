package com.lushihao.picture.domain.space.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lushihao.picture.domain.space.entity.SpaceUser;
import com.lushihao.picture.domain.space.repository.SpaceUserRepository;
import com.lushihao.picture.domain.space.service.SpaceUserDomainService;
import com.lushihao.picture.infrastructure.exception.ErrorCode;
import com.lushihao.picture.infrastructure.exception.ThrowUtils;
import com.lushihao.picture.interfaces.dto.spaceuser.SpaceUserQueryRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lushihao
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
 * @createDate 2025-08-06 14:29:25
 */
@Service
public class SpaceUserDomainServiceImpl implements SpaceUserDomainService {

    @Resource
    private SpaceUserRepository spaceUserRepository;


    @Override
    public boolean save(SpaceUser spaceUser) {
        return spaceUserRepository.save(spaceUser);
    }

    /**
     * 根据查询条件古剑LambdaQueryWrapper对象
     *
     * @param spaceUserQueryRequest 查询条件
     * @return LambdaQueryWrapper对象
     */
    @Override
    public LambdaQueryWrapper<SpaceUser> getLambdaQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);

        Long id = spaceUserQueryRequest.getId();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        String spaceRole = spaceUserQueryRequest.getSpaceRole();

        LambdaQueryWrapper<SpaceUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(id), SpaceUser::getId, id);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(spaceId), SpaceUser::getSpaceId, spaceId);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(userId), SpaceUser::getUserId, userId);
        lambdaQueryWrapper.eq(StrUtil.isNotEmpty(spaceRole), SpaceUser::getSpaceRole, spaceRole);

        return lambdaQueryWrapper;
    }


    @Override
    public SpaceUser getByUserIdAndSpaceId(Long userId, Long spaceId) {
        return spaceUserRepository.lambdaQuery().eq(SpaceUser::getSpaceId, spaceId).eq(SpaceUser::getUserId, userId).one();
    }

    @Override
    public boolean removeById(long spaceId) {
        return spaceUserRepository.removeById(spaceId);
    }

    @Override
    public SpaceUser getById(long spaceId) {
        return spaceUserRepository.getById(spaceId);
    }

    @Override
    public SpaceUser getOne(LambdaQueryWrapper<SpaceUser> lambdaQueryWrapper) {
        return spaceUserRepository.getOne(lambdaQueryWrapper);
    }

    @Override
    public List<SpaceUser> list(LambdaQueryWrapper<SpaceUser> lambdaQueryWrapper) {
        return spaceUserRepository.list(lambdaQueryWrapper);
    }

    @Override
    public boolean updateById(SpaceUser spaceUser) {
        return spaceUserRepository.updateById(spaceUser);
    }

    @Override
    public SpaceUser getSpaceUserBySpaceIdAndUserId(Long spaceId, Long userId) {
        return spaceUserRepository.lambdaQuery()
                .eq(SpaceUser::getSpaceId, spaceId)
                .eq(SpaceUser::getUserId, userId)
                .one();
    }


}




