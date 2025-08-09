package com.lushihao.picture.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.picture.domain.picture.entity.PictureLike;
import com.lushihao.picture.domain.picture.repository.PictureLikeRepository;
import com.lushihao.picture.domain.space.entity.Space;
import com.lushihao.picture.domain.space.repository.SpaceRepository;
import com.lushihao.picture.infrastructure.mapper.PictureLikeMapper;
import com.lushihao.picture.infrastructure.mapper.SpaceMapper;
import org.springframework.stereotype.Service;

/**
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-09   9:12
 */
@Service
public class PictureLikeRepositoryImpl extends ServiceImpl<PictureLikeMapper, PictureLike> implements PictureLikeRepository {
}
