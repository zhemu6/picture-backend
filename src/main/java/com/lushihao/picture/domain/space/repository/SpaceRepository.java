package com.lushihao.picture.domain.space.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lushihao.picture.domain.space.entity.Space;

/**
 * 空间仓储
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-08   20:28
 */
public interface SpaceRepository extends IService<Space> {
    boolean addUpdatePicture(Long finalSpaceId, Long picSize);

    boolean deleteUpdatePicture(Long spaceId, Long picSize);
}
