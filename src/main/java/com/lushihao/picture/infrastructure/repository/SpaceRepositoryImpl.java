package com.lushihao.picture.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.picture.domain.space.entity.Space;
import com.lushihao.picture.domain.space.repository.SpaceRepository;
import com.lushihao.picture.infrastructure.mapper.SpaceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-08   20:29
 */
@Service
public class SpaceRepositoryImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceRepository {

    @Resource
    private SpaceMapper spaceMapper;

    @Override
    public boolean addUpdatePicture(Long finalSpaceId, Long picSize) {
        return spaceMapper.update(null,
                new LambdaUpdateWrapper<Space>()
                        .eq(Space::getId, finalSpaceId)
                        .setSql("total_size = total_size + " + picSize)
                        .setSql("total_count = total_count + 1")
        ) > 0;
    }

    @Override
    public boolean deleteUpdatePicture(Long spaceId, Long picSize) {
        return spaceMapper.update(null,
                new LambdaUpdateWrapper<Space>()
                        .eq(Space::getId, spaceId)
                        .setSql("total_size = total_size - " + picSize)
                        .setSql("total_count = total_count - 1")
        ) > 0;
    }

}
