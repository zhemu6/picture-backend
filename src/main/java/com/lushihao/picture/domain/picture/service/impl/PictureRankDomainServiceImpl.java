package com.lushihao.picture.domain.picture.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lushihao.picture.domain.picture.entity.PictureRank;
import com.lushihao.picture.domain.picture.repository.PictureLikeRepository;
import com.lushihao.picture.domain.picture.repository.PictureRankRepository;
import com.lushihao.picture.domain.picture.service.PictureRankDomainService;
import com.lushihao.picture.interfaces.dto.picture.PictureRankCount;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片点赞的领域服务层
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-10   16:19
 */
@Service
public class PictureRankDomainServiceImpl implements PictureRankDomainService {
    @Resource
    private PictureLikeRepository pictureLikeRepository;
    @Resource
    private PictureRankRepository pictureRankRepository;

    /**
     * 删除旧的数据 并且获得前十的图片的id 和点赞数
     *
     * @param type      day week month
     * @param startTime 开始时间
     * @param endTime   结束时间
     */
    @Override
    public void generatePictureRank(String type, LocalDateTime startTime, LocalDateTime endTime, int limit) {
        // 删除旧的榜单的数据
        pictureRankRepository.deleteByTypeAndTimeRange(type, startTime, endTime);
        // 查询当前点赞数较高的图片列表
        List<PictureRankCount> topList = pictureLikeRepository.findTopPicturesByLikeCount(startTime, endTime, limit);

        List<PictureRank> rankList = new ArrayList<>();
        int rankNum = 1;
        for (PictureRankCount item : topList) {
            PictureRank rank = new PictureRank();
            rank.setType(type);
            rank.setPictureId(item.getPictureId());
            rank.setLikeCount(item.getLikeCount());
            rank.setRankNum(rankNum++);
            rank.setStartTime(startTime);
            rank.setEndTime(endTime);
            rank.setIsDelete(0);
            rankList.add(rank);
        }
        // 保存榜单
        pictureRankRepository.saveBatch(rankList);
    }

    @Override
    public List<PictureRank> getRankByType(String type) {
        return pictureRankRepository.getRankByType(type);

    }


}
