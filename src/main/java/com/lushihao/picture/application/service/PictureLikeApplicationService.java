package com.lushihao.picture.application.service;

import com.lushihao.picture.domain.picture.entity.Picture;
import com.lushihao.picture.domain.picture.entity.PictureLike;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lushihao.picture.domain.user.entity.User;

/**
* @author lushihao
* @description 针对表【picture_like(图片点赞表)】的数据库操作Service
* @createDate 2025-08-01 20:27:05
*/
public interface PictureLikeApplicationService  {
    /**
     * 点赞图片
     * @param pictureId 图片点赞请求
     * @param loginUser 登录用户
     * @return 是否点赞成功
     */
    Boolean likePicture(Long pictureId, User loginUser);

    /**
     * 获取图片点赞数
     * @param pictureId 图片的id
     * @return 点赞数
     */
    Long countByPictureId(Long pictureId);

    boolean hasUserLiked(Long userId, Long pictureId);

}
