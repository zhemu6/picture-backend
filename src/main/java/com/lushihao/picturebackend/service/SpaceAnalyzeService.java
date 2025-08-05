package com.lushihao.picturebackend.service;

import com.lushihao.picturebackend.model.dto.space.analyze.*;
import com.lushihao.picturebackend.model.entity.Space;
import com.lushihao.picturebackend.model.vo.space.analye.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-05   8:47
 */
public interface SpaceAnalyzeService {
    /**
     * 获取空间的使用情况
     *
     * @param spaceUsageAnalyzeRequest 空间使用情况请求
     * @param request                  获取用户信息
     * @return spaceUsageAnalyzeResponse
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, HttpServletRequest request);

    /**
     * 获取空间中图片的分类统计情况
     *
     * @param spaceCategoryAnalyzeRequest 图片类别统计请求
     * @param request                     获取用户信息
     * @return SpaceCategoryAnalyzeResponse的list集合
     */
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, HttpServletRequest request);
    /**
     * 获取空间中图片的分标签统计情况
     * @param spaceTagAnalyzeRequest 图片分标签统计情况
     * @param request 获取用户信息
     * @return SpaceTagAnalyzeResponse集合 包含每个标签的名称数量
     */
    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, HttpServletRequest request);
    /**
     * 获取用户在时间维度上上传图片的统计情况
     * @param spaceUserAnalyzeRequest 用户行为统计
     * @param request 获取用户信息
     * @return SpaceUserAnalyzeResponse集合 包含每个时间点的上传数量
     */
    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, HttpServletRequest request);
    /**
     * 取出前10个空间 （用量）的统计情况
     * @param spaceRankAnalyzeRequest 空间排名请求
     * @param request 获取用户信息
     * @return Space集合  返回前topk个占用space
     */
    List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, HttpServletRequest request);
    /**
     * 获取图片不同大小范围上的数量分布
     * @param spaceSizeAnalyzeRequest 空间区间请求
     * @param request 获取用户信息
     * @return SpaceSizeAnalyzeResponse集合  返回图片不同大小范围上的数量分布
     */
    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, HttpServletRequest request);
}
