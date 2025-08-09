package com.lushihao.picture.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.picture.domain.picture.entity.PictureComment;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.infrastructure.common.DeleteRequest;
import com.lushihao.picture.interfaces.dto.picture.PictureCommentAddRequest;
import com.lushihao.picture.interfaces.dto.picture.PictureCommentQueryRequest;
import com.lushihao.picture.interfaces.vo.picture.PictureCommentVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author lushihao
* @description 针对表【picture_comment(图片评论表（支持楼中楼）)】的数据库操作Service
* @createDate 2025-08-01 20:27:36
*/
public interface PictureCommentApplicationService {
    /**
     * 创建评论
     *
     * @param pictureCommentAddRequest 发表评论请求
     * @param loginUser                  用于获取登陆用户
     * @return 创建的评论id
     */
    long addPictureComment(PictureCommentAddRequest pictureCommentAddRequest, User loginUser);

    /**
     * 删除评论
     *
     * @param deleteRequest 删除请求
     * @param loginUser       获取登录用户
     * @return 是否删除成功
     */
    boolean deletePictureComment(DeleteRequest deleteRequest, User loginUser);


    LambdaQueryWrapper<PictureComment> getLambdaQueryWrapper(PictureCommentQueryRequest pictureCommentQueryRequest);

    Page<PictureCommentVO> getPictureCommentVOPage(Page<PictureComment> pictureCommentPage, HttpServletRequest request);

    Page<PictureComment> page(Page<PictureComment> pictureCommentPage, LambdaQueryWrapper<PictureComment> lambdaQueryWrapper);
}
