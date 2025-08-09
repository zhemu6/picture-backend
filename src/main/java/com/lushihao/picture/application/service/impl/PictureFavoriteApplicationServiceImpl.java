package com.lushihao.picture.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.picture.domain.picture.service.impl.PictureFavoriteDomainServiceImpl;
import com.lushihao.picture.infrastructure.exception.ErrorCode;
import com.lushihao.picture.infrastructure.exception.ThrowUtils;
import com.lushihao.picture.domain.picture.entity.PictureFavorite;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.application.service.PictureFavoriteApplicationService;
import com.lushihao.picture.infrastructure.mapper.PictureFavoriteMapper;
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
public class PictureFavoriteApplicationServiceImpl implements PictureFavoriteApplicationService {

    @Resource
    private PictureFavoriteDomainServiceImpl pictureFavoriteDomainServiceImpl;

    @Override
    public Boolean favoritePicture(Long pictureId, User loginUser) {
       return  pictureFavoriteDomainServiceImpl.favoritePicture(pictureId, loginUser);
    }

    @Override
    public Long countByPictureId(Long pictureId) {
        return  pictureFavoriteDomainServiceImpl.countByPictureId(pictureId);
    }

    @Override
    public boolean hasUserFavorite(Long userId, Long pictureId) {
        return pictureFavoriteDomainServiceImpl.hasUserFavorite(userId,pictureId);
    }
}




