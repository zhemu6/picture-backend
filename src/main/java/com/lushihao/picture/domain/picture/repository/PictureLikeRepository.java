package com.lushihao.picture.domain.picture.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lushihao.picture.domain.picture.entity.PictureLike;
import com.lushihao.picture.interfaces.dto.picture.PictureRankCount;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-09   9:12
 */
public interface PictureLikeRepository extends IService<PictureLike> {
    List<PictureRankCount> findTopPicturesByLikeCount(LocalDateTime startTime, LocalDateTime endTime, int limit);
}
