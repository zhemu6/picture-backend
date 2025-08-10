package com.lushihao.picture.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.picture.domain.picture.entity.PictureLike;
import com.lushihao.picture.domain.picture.repository.PictureLikeRepository;
import com.lushihao.picture.infrastructure.mapper.PictureLikeMapper;
import com.lushihao.picture.interfaces.dto.picture.PictureRankCount;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-09   9:12
 */
@Service
public class PictureLikeRepositoryImpl extends ServiceImpl<PictureLikeMapper, PictureLike> implements PictureLikeRepository {
    @Resource
    private PictureLikeMapper pictureLikeMapper;

    /**
     * 获得图片的点赞数
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param limit     条数
     * @return PictureRankCount列表
     */
    @Override
    public List<PictureRankCount> findTopPicturesByLikeCount(LocalDateTime startTime, LocalDateTime endTime, int limit) {
        return pictureLikeMapper.getTopLikedPictures(startTime, endTime, limit);
    }
}
