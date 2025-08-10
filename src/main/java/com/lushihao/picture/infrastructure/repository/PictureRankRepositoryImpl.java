package com.lushihao.picture.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.picture.domain.picture.entity.PictureRank;
import com.lushihao.picture.domain.picture.repository.PictureRankRepository;
import com.lushihao.picture.infrastructure.mapper.PictureRankMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-10   16:17
 */
@Service
public class PictureRankRepositoryImpl extends ServiceImpl<PictureRankMapper, PictureRank> implements PictureRankRepository {
    @Resource
    private PictureRankMapper pictureRankMapper;

    @Override
    public void saveBatch(List<PictureRank> ranks) {
        List<PictureRank> dos = ranks.stream().map(rank -> {
            PictureRank doObj = new PictureRank();
            doObj.setType(rank.getType());
            doObj.setPictureId(rank.getPictureId());
            doObj.setLikeCount(rank.getLikeCount());
            doObj.setRankNum(rank.getRankNum());
            doObj.setStartTime(rank.getStartTime());
            doObj.setEndTime(rank.getEndTime());
            doObj.setIsDelete(rank.getIsDelete());
            return doObj;
        }).collect(Collectors.toList());

        dos.forEach(pictureRankMapper::insert);
    }

    @Override
    public void deleteByTypeAndTimeRange(String type, LocalDateTime startTime, LocalDateTime endTime) {
        pictureRankMapper.delete(new LambdaQueryWrapper<PictureRank>()
                .eq(PictureRank::getType, type)
                .eq(PictureRank::getStartTime, startTime)
                .eq(PictureRank::getEndTime, endTime));
    }

    @Override
    public List<PictureRank> getRankByType(String type) {
        return pictureRankMapper.selectList(
                new LambdaQueryWrapper<PictureRank>()
                        .eq(PictureRank::getType, type)
                        .eq(PictureRank::getIsDelete, 0)
                        .orderByAsc(PictureRank::getRankNum)
                        .last("LIMIT 10")
        );
    }
}
