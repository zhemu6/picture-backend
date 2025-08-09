package com.lushihao.picture.interfaces.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.picture.application.service.PictureCommentApplicationService;
import com.lushihao.picture.application.service.UserApplicationService;
import com.lushihao.picture.domain.picture.entity.PictureComment;
import com.lushihao.picture.domain.user.constant.UserConstant;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.infrastructure.annotation.AuthCheck;
import com.lushihao.picture.infrastructure.common.BaseResponse;
import com.lushihao.picture.infrastructure.common.DeleteRequest;
import com.lushihao.picture.infrastructure.common.ResultUtils;
import com.lushihao.picture.infrastructure.exception.ErrorCode;
import com.lushihao.picture.infrastructure.exception.ThrowUtils;
import com.lushihao.picture.interfaces.dto.picture.PictureCommentAddRequest;
import com.lushihao.picture.interfaces.dto.picture.PictureCommentQueryRequest;
import com.lushihao.picture.interfaces.vo.picture.PictureCommentVO;
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
    private PictureCommentApplicationService pictureCommentApplicationService;

    @Resource
    private UserApplicationService userApplicationService;
    /**
     * 创建评论
     *
     * @param pictureCommentAddRequest 发表评论请求
     * @param request                  用于获取登陆用户
     * @return 创建的评论id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addPictureComment(@RequestBody PictureCommentAddRequest pictureCommentAddRequest, HttpServletRequest request) {
        User loginUser = userApplicationService.getLoginUser(request);
        long newCommentId = pictureCommentApplicationService.addPictureComment(pictureCommentAddRequest, loginUser);
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
        User loginUser = userApplicationService.getLoginUser(request);
        boolean result = pictureCommentApplicationService.deletePictureComment(deleteRequest, loginUser);
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
        Page<PictureComment> pictureCommentPage = pictureCommentApplicationService.page(new Page<>(current,size), pictureCommentApplicationService.getLambdaQueryWrapper(pictureCommentQueryRequest));
        return ResultUtils.success(pictureCommentApplicationService.getPictureCommentVOPage(pictureCommentPage,request));
    }



}
