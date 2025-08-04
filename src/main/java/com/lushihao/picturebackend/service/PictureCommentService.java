package com.lushihao.picturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.picturebackend.common.DeleteRequest;
import com.lushihao.picturebackend.model.dto.pictureComment.PictureCommentAddRequest;
import com.lushihao.picturebackend.model.dto.pictureComment.PictureCommentQueryRequest;
import com.lushihao.picturebackend.model.entity.PictureComment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lushihao.picturebackend.model.vo.PictureCommentVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author lushihao
* @description 针对表【picture_comment(图片评论表（支持楼中楼）)】的数据库操作Service
* @createDate 2025-08-01 20:27:36
*/
public interface PictureCommentService extends IService<PictureComment> {
    /**
     * 创建评论
     *
     * @param pictureCommentAddRequest 发表评论请求
     * @param request                  用于获取登陆用户
     * @return 创建的评论id
     */
    long addPictureComment(PictureCommentAddRequest pictureCommentAddRequest, HttpServletRequest request);

    /**
     * 删除评论
     *
     * @param deleteRequest 删除请求
     * @param request       获取登录用户
     * @return 是否删除成功
     */
    boolean deletePictureComment(DeleteRequest deleteRequest, HttpServletRequest request);


    LambdaQueryWrapper<PictureComment> getLambdaQueryWrapper(PictureCommentQueryRequest pictureCommentQueryRequest);

    Page<PictureCommentVO> getPictureCommentVOPage(Page<PictureComment> pictureCommentPage, HttpServletRequest request);
}
