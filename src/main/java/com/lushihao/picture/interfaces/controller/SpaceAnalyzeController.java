package com.lushihao.picture.interfaces.controller;

import com.lushihao.picture.infrastructure.common.BaseResponse;
import com.lushihao.picture.infrastructure.common.ResultUtils;
import com.lushihao.picture.infrastructure.exception.ErrorCode;
import com.lushihao.picture.infrastructure.exception.ThrowUtils;
import com.lushihao.picture.interfaces.dto.space.analyze.*;
import com.lushihao.picture.interfaces.vo.space.analye.*;
import com.lushihao.picture.domain.space.entity.Space;
import com.lushihao.picture.application.service.SpaceAnalyzeApplicationService;
import com.lushihao.picture.application.service.SpaceApplicationService;
import com.lushihao.picture.application.service.UserApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 空间分析控制层
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-02   20:54
 */
@Slf4j
@RequestMapping("/space/analyze")
@RestController
public class SpaceAnalyzeController {

    @Resource
    private SpaceAnalyzeApplicationService spaceAnalyzeApplicationService;

    /**
     * 获取空间的使用情况
     * @param spaceUsageAnalyzeRequest 空间使用情况请求
     * @param request 获取用户信息
     * @return spaceUsageAnalyzeResponse
     */
    @PostMapping("/usage")
    public BaseResponse<SpaceUsageAnalyzeResponse> getSpaceUsageAnalyze(@RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest==null, ErrorCode.PARAMS_ERROR);
        SpaceUsageAnalyzeResponse response = spaceAnalyzeApplicationService.getSpaceUsageAnalyze(spaceUsageAnalyzeRequest, request);
        return ResultUtils.success(response);
    }

    /**
     * 获取空间中图片的分类统计情况
     * @param spaceCategoryAnalyzeRequest 图片类别统计请求
     * @param request 获取用户信息
     * @return SpaceCategoryAnalyzeResponse的list集合
     */
    @PostMapping("/category")
    public BaseResponse<List<SpaceCategoryAnalyzeResponse>> getSpaceCategoryAnalyze(@RequestBody SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest==null, ErrorCode.PARAMS_ERROR);
        List<SpaceCategoryAnalyzeResponse> spaceCategoryAnalyzeResponseList= spaceAnalyzeApplicationService.getSpaceCategoryAnalyze(spaceCategoryAnalyzeRequest, request);
        return ResultUtils.success(spaceCategoryAnalyzeResponseList);
    }

    /**
     * 获取空间中图片的分标签统计情况
     * @param spaceTagAnalyzeRequest 图片分标签统计情况
     * @param request 获取用户信息
     * @return SpaceTagAnalyzeResponse集合 包含每个标签的名称数量
     */
    @PostMapping("/tag")
    public BaseResponse<List<SpaceTagAnalyzeResponse>> getSpaceTagAnalyze(@RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest==null, ErrorCode.PARAMS_ERROR);
        List<SpaceTagAnalyzeResponse> spaceTagAnalyzeResponseList= spaceAnalyzeApplicationService.getSpaceTagAnalyze(spaceTagAnalyzeRequest, request);
        return ResultUtils.success(spaceTagAnalyzeResponseList);
    }

    /**
     * 获取图片不同大小范围上的数量分布
     * @param spaceSizeAnalyzeRequest 空间区间请求
     * @param request 获取用户信息
     * @return SpaceSizeAnalyzeResponse集合  返回图片不同大小范围上的数量分布
     */
    @PostMapping("/size")
    public BaseResponse<List<SpaceSizeAnalyzeResponse>> getSpaceSizeAnalyze(@RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest==null, ErrorCode.PARAMS_ERROR);
        List<SpaceSizeAnalyzeResponse> spaceSizeAnalyzeResponseList= spaceAnalyzeApplicationService.getSpaceSizeAnalyze(spaceSizeAnalyzeRequest, request);
        return ResultUtils.success(spaceSizeAnalyzeResponseList);
    }


    /**
     * 获取用户在时间维度上上传图片的统计情况
     * @param spaceUserAnalyzeRequest 用户行为统计
     * @param request 获取用户信息
     * @return SpaceUserAnalyzeResponse集合 包含每个时间点的上传数量
     */
    @PostMapping("/user")
    public BaseResponse<List<SpaceUserAnalyzeResponse>> getSpaceUserAnalyze(@RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest==null, ErrorCode.PARAMS_ERROR);
        List<SpaceUserAnalyzeResponse> spaceUserAnalyzeResponseList= spaceAnalyzeApplicationService.getSpaceUserAnalyze(spaceUserAnalyzeRequest, request);
        return ResultUtils.success(spaceUserAnalyzeResponseList);
    }

    /**
     * 取出前10个空间 （用量）的统计情况
     * @param spaceRankAnalyzeRequest 空间排名请求
     * @param request 获取用户信息
     * @return Space集合  返回前topk个占用space
     */
    @PostMapping("/rank")
    public BaseResponse<List<Space>> getSpaceRankAnalyze(@RequestBody SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceRankAnalyzeRequest==null, ErrorCode.PARAMS_ERROR);
        List<Space> spaceList= spaceAnalyzeApplicationService.getSpaceRankAnalyze(spaceRankAnalyzeRequest, request);
        return ResultUtils.success(spaceList);
    }

}
