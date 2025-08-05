package com.lushihao.picturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.picturebackend.annotation.AuthCheck;
import com.lushihao.picturebackend.common.BaseResponse;
import com.lushihao.picturebackend.common.DeleteRequest;
import com.lushihao.picturebackend.common.ResultUtils;
import com.lushihao.picturebackend.constant.UserConstant;
import com.lushihao.picturebackend.exception.ErrorCode;
import com.lushihao.picturebackend.exception.ThrowUtils;
import com.lushihao.picturebackend.model.dto.space.*;
import com.lushihao.picturebackend.model.dto.space.analyze.*;
import com.lushihao.picturebackend.model.entity.Space;
import com.lushihao.picturebackend.model.entity.User;
import com.lushihao.picturebackend.model.enums.SpaceLevelEnum;
import com.lushihao.picturebackend.model.vo.SpaceVO;
import com.lushihao.picturebackend.model.vo.space.analye.*;
import com.lushihao.picturebackend.service.SpaceAnalyzeService;
import com.lushihao.picturebackend.service.SpaceService;
import com.lushihao.picturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
    private SpaceService spaceService;

    @Resource
    private UserService userService;
    @Resource
    private SpaceAnalyzeService spaceAnalyzeService;

    /**
     * 获取空间的使用情况
     * @param spaceUsageAnalyzeRequest 空间使用情况请求
     * @param request 获取用户信息
     * @return spaceUsageAnalyzeResponse
     */
    @PostMapping("/usage")
    public BaseResponse<SpaceUsageAnalyzeResponse> getSpaceUsageAnalyze(@RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest==null, ErrorCode.PARAMS_ERROR);
        SpaceUsageAnalyzeResponse response = spaceAnalyzeService.getSpaceUsageAnalyze(spaceUsageAnalyzeRequest, request);
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
        List<SpaceCategoryAnalyzeResponse> spaceCategoryAnalyzeResponseList= spaceAnalyzeService.getSpaceCategoryAnalyze(spaceCategoryAnalyzeRequest, request);
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
        List<SpaceTagAnalyzeResponse> spaceTagAnalyzeResponseList= spaceAnalyzeService.getSpaceTagAnalyze(spaceTagAnalyzeRequest, request);
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
        List<SpaceSizeAnalyzeResponse> spaceSizeAnalyzeResponseList= spaceAnalyzeService.getSpaceSizeAnalyze(spaceSizeAnalyzeRequest, request);
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
        List<SpaceUserAnalyzeResponse> spaceUserAnalyzeResponseList= spaceAnalyzeService.getSpaceUserAnalyze(spaceUserAnalyzeRequest, request);
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
        List<Space> spaceList= spaceAnalyzeService.getSpaceRankAnalyze(spaceRankAnalyzeRequest, request);
        return ResultUtils.success(spaceList);
    }

}
