package com.lushihao.picturebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.picturebackend.annotation.AuthCheck;
import com.lushihao.picturebackend.common.BaseResponse;
import com.lushihao.picturebackend.common.DeleteRequest;
import com.lushihao.picturebackend.common.ResultUtils;
import com.lushihao.picturebackend.constant.UserConstant;
import com.lushihao.picturebackend.exception.ErrorCode;
import com.lushihao.picturebackend.exception.ThrowUtils;
import com.lushihao.picturebackend.model.dto.pictureComment.PictureCommentAddRequest;
import com.lushihao.picturebackend.model.dto.pictureComment.PictureCommentQueryRequest;
import com.lushihao.picturebackend.model.dto.space.SpaceQueryRequest;
import com.lushihao.picturebackend.model.entity.PictureComment;
import com.lushihao.picturebackend.model.entity.Space;
import com.lushihao.picturebackend.model.vo.PictureCommentVO;
import com.lushihao.picturebackend.model.vo.SpaceVO;
import com.lushihao.picturebackend.service.PictureCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-04   21:39
 */

@Slf4j
@RequestMapping("/pictureComment")
@RestController
public class PictureCommentController {

    @Resource
    private PictureCommentService pictureCommentService;


    /**
     * 创建评论
     *
     * @param pictureCommentAddRequest 发表评论请求
     * @param request                  用于获取登陆用户
     * @return 创建的评论id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addPictureComment(@RequestBody PictureCommentAddRequest pictureCommentAddRequest, HttpServletRequest request) {
        long newCommentId = pictureCommentService.addPictureComment(pictureCommentAddRequest, request);
        return ResultUtils.success(newCommentId);
    }

    /**
     * 删除评论
     *
     * @param deleteRequest 删除请求
     * @param request       获取登录用户
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deletePictureComment(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        boolean result = pictureCommentService.deletePictureComment(deleteRequest, request);
        return ResultUtils.success(result);
    }


    /**
     * 分页获取空间列表（封装类） 如果parentid 为0 查当前图片的所有父评论 如果为其他值  并传入pictureid 则是查询当前这个图片下的所有子评论
     * 提供给普通用户
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureCommentVO>> listPictureCommentVOByPage(@RequestBody PictureCommentQueryRequest pictureCommentQueryRequest, HttpServletRequest request){
        long current = pictureCommentQueryRequest.getCurrent();
        long size = pictureCommentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size>20, ErrorCode.PARAMS_ERROR);
        // 查询数据库 获得pictureComment page对象转成vo即可 这里穿回来的查询条件中
        Page<PictureComment> pictureCommentPage = pictureCommentService.page(new Page<>(current,size),pictureCommentService.getLambdaQueryWrapper(pictureCommentQueryRequest));
        return ResultUtils.success(pictureCommentService.getPictureCommentVOPage(pictureCommentPage,request));
    }



}
