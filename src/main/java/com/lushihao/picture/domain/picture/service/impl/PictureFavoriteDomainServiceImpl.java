package com.lushihao.picture.domain.picture.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lushihao.picture.application.service.PictureFavoriteApplicationService;
import com.lushihao.picture.domain.picture.entity.PictureFavorite;
import com.lushihao.picture.domain.picture.repository.PictureFavoriteRepository;
import com.lushihao.picture.domain.picture.service.PictureFavoriteDomainService;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.infrastructure.exception.ErrorCode;
import com.lushihao.picture.infrastructure.exception.ThrowUtils;
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
public class PictureFavoriteDomainServiceImpl implements PictureFavoriteDomainService {

    @Resource
    private PictureFavoriteRepository pictureFavoriteRepository;

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

        PictureFavorite existing = pictureFavoriteRepository.getOne(pictureFavoriteLambdaQueryWrapper);
        // 如果查询到

        if (existing != null) {
            boolean isCurrentlyFavorited = existing.getIsDelete() == 0;
            existing.setIsDelete(isCurrentlyFavorited ? 1 : 0);
            return pictureFavoriteRepository.updateById(existing);
        }
        // 未收藏 -> 收藏 insert为1 代表已收藏
        PictureFavorite pictureFavorite = new PictureFavorite();
        pictureFavorite.setUserId(userId);
        pictureFavorite.setPictureId(pictureId);
        return pictureFavoriteRepository.save(pictureFavorite);
    }

    @Override
    public Long countByPictureId(Long pictureId) {
        return pictureFavoriteRepository.lambdaQuery().eq(PictureFavorite::getPictureId, pictureId).eq(PictureFavorite::getIsDelete, 0).count();
    }

    @Override
    public boolean hasUserFavorite(Long userId, Long pictureId) {
        return pictureFavoriteRepository.lambdaQuery()
                .eq(PictureFavorite::getUserId, userId)
                .eq(PictureFavorite::getPictureId, pictureId)
                .eq(PictureFavorite::getIsDelete, 0)
                .exists(); // 或 count() > 0
    }
}




