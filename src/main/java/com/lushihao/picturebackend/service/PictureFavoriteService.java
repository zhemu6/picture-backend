package com.lushihao.picturebackend.service;

import com.lushihao.picturebackend.model.dto.pictureFavorite.PictureFavoriteRequest;
import com.lushihao.picturebackend.model.entity.PictureFavorite;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lushihao.picturebackend.model.entity.User;

/**
* @author lushihao
* @description 针对表【picture_favorite(图片收藏表)】的数据库操作Service
* @createDate 2025-08-01 20:27:33
*/
public interface PictureFavoriteService extends IService<PictureFavorite> {
    /**
     * 收藏图片
     * @param pictureId 图片id
     * @param loginUser 登录用户
     * @return 是否收藏成功
     */
    Boolean favoritePicture(Long pictureId, User loginUser);


    /**
     * 获取图片收藏数
     * @param pictureId 图片的id
     * @return 收藏数
     */
    Long countByPictureId(Long pictureId);

    /**
     * 用户是否收藏
     * @param userId 用户id
     * @param pictureId 图片id
     * @return 是否收藏
     */
    boolean hasUserFavorite(Long userId, Long pictureId);

}
