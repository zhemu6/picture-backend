package com.lushihao.picture.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.picture.domain.picture.entity.PictureFavorite;
import com.lushihao.picture.domain.picture.repository.PictureFavoriteRepository;
import com.lushihao.picture.domain.space.repository.SpaceRepository;
import com.lushihao.picture.infrastructure.mapper.PictureFavoriteMapper;
import com.lushihao.picture.infrastructure.mapper.SpaceMapper;
import org.springframework.stereotype.Service;

/**
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-09   9:13
 */
@Service
public class PictureFavoriteRepositoryImpl extends ServiceImpl<PictureFavoriteMapper, PictureFavorite> implements PictureFavoriteRepository {
}
