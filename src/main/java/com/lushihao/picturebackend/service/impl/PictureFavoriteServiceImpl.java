package com.lushihao.picturebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.picturebackend.exception.ErrorCode;
import com.lushihao.picturebackend.exception.ThrowUtils;
import com.lushihao.picturebackend.model.entity.Picture;
import com.lushihao.picturebackend.model.entity.PictureFavorite;
import com.lushihao.picturebackend.model.entity.User;
import com.lushihao.picturebackend.service.PictureFavoriteService;
import com.lushihao.picturebackend.mapper.PictureFavoriteMapper;
import com.lushihao.picturebackend.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author lushihao
* @description 针对表【picture_favorite(图片收藏表)】的数据库操作Service实现
* @createDate 2025-08-01 20:27:33
*/
@Service
@Slf4j
public class PictureFavoriteServiceImpl extends ServiceImpl<PictureFavoriteMapper, PictureFavorite>
    implements PictureFavoriteService{

    @Override
    public Boolean favoritePicture(Long pictureId, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 获取用户id和图片id
        Long userId = loginUser.getId();
        // 查询用户是否已经收藏
        LambdaQueryWrapper<PictureFavorite> pictureFavoriteLambdaQueryWrapper = new LambdaQueryWrapper<>();
        pictureFavoriteLambdaQueryWrapper
                .eq(PictureFavorite::getUserId, userId)
                .eq(PictureFavorite::getPictureId, pictureId);

        PictureFavorite existing = this.getOne(pictureFavoriteLambdaQueryWrapper);
        // 如果查询到

        if (existing != null) {
            boolean isCurrentlyFavorited = existing.getIsDelete() == 0;
            existing.setIsDelete(isCurrentlyFavorited ? 1 : 0);
            return this.updateById(existing);
        }
        // 未收藏 -> 收藏 insert为1 代表已收藏
        PictureFavorite pictureFavorite = new PictureFavorite();
        pictureFavorite.setUserId(userId);
        pictureFavorite.setPictureId(pictureId);
        return this.save(pictureFavorite);
    }

    @Override
    public Long countByPictureId(Long pictureId) {
        return this.lambdaQuery().eq(PictureFavorite::getPictureId, pictureId).eq(PictureFavorite::getIsDelete, 0).count();
    }

    @Override
    public boolean hasUserFavorite(Long userId, Long pictureId) {
        return this.lambdaQuery()
                .eq(PictureFavorite::getUserId, userId)
                .eq(PictureFavorite::getPictureId, pictureId)
                .eq(PictureFavorite::getIsDelete, 0)
                .exists(); // 或 count() > 0
    }
}




