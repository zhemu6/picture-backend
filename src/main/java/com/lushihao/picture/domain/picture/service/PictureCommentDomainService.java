package com.lushihao.picture.domain.picture.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.picture.domain.picture.entity.PictureComment;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.infrastructure.common.DeleteRequest;
import com.lushihao.picture.interfaces.dto.picture.PictureCommentQueryRequest;

/**
* @author lushihao
* @description 针对表【picture_comment(图片评论表（支持楼中楼）)】的数据库操作Service
* @createDate 2025-08-01 20:27:36
*/
public interface PictureCommentDomainService {

    /**
     * 删除评论
     *
     * @param deleteRequest 删除请求
     * @param lgoinUser       获取登录用户
     * @return 是否删除成功
     */
    boolean deletePictureComment(DeleteRequest deleteRequest, User lgoinUser);

    LambdaQueryWrapper<PictureComment> getLambdaQueryWrapper(PictureCommentQueryRequest pictureCommentQueryRequest);

    PictureComment getById(Long parentId);

    boolean save(PictureComment pictureComment);

    Page<PictureComment> page(Page<PictureComment> pictureCommentPage, LambdaQueryWrapper<PictureComment> lambdaQueryWrapper);
}
