package com.lushihao.picture.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.picture.domain.space.entity.SpaceUser;
import com.lushihao.picture.domain.space.repository.SpaceUserRepository;
import com.lushihao.picture.infrastructure.mapper.SpaceUserMapper;
import org.springframework.stereotype.Service;

/**
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-08   20:31
 */
@Service
public class SpaceUserRepositoryImpl extends ServiceImpl<SpaceUserMapper, SpaceUser> implements SpaceUserRepository {
}
