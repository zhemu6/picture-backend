package com.lushihao.picture.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.picture.domain.picture.entity.PictureComment;
import com.lushihao.picture.domain.picture.repository.PictureCommentRepository;
import com.lushihao.picture.domain.space.repository.SpaceRepository;
import com.lushihao.picture.infrastructure.mapper.PictureCommentMapper;
import com.lushihao.picture.infrastructure.mapper.SpaceMapper;
import org.springframework.stereotype.Service;

/**
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-09   9:14
 */
@Service
public class PictureCommentRepositoryImpl extends ServiceImpl<PictureCommentMapper, PictureComment> implements PictureCommentRepository {
}
