package com.lushihao.picture.domain.picture.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lushihao.picture.domain.picture.entity.PictureRank;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 图片排行仓储
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-10   16:14
 */
public interface PictureRankRepository extends IService<PictureRank> {
    /**
     * 批量保存榜单
     */
    void saveBatch(List<PictureRank> ranks);

    /**
     * 删除指定类型和时间区间的榜单数据
     */
    void deleteByTypeAndTimeRange(String type, LocalDateTime startTime, LocalDateTime endTime);


    List<PictureRank> getRankByType(String type);
}
