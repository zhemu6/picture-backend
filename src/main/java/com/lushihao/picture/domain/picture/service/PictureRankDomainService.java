package com.lushihao.picture.domain.picture.service;

import com.lushihao.picture.domain.picture.entity.PictureRank;
import com.lushihao.picture.interfaces.dto.picture.PictureRankCount;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-10   16:19
 */
public interface PictureRankDomainService {

    void generatePictureRank(String type, LocalDateTime startTime, LocalDateTime endTime, int limit);


    List<PictureRank> getRankByType(String type);
}
