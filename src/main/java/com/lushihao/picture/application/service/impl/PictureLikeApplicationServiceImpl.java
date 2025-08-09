package com.lushihao.picture.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.picture.domain.picture.service.PictureLikeDomainService;
import com.lushihao.picture.infrastructure.exception.ErrorCode;
import com.lushihao.picture.infrastructure.exception.ThrowUtils;
import com.lushihao.picture.domain.picture.entity.PictureLike;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.application.service.PictureLikeApplicationService;
import com.lushihao.picture.infrastructure.mapper.PictureLikeMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author lushihao
 * @description 针对表【picture_like(图片点赞表)】的数据库操作Service实现
 * @createDate 2025-08-01 20:27:05
 */
@Service
public class PictureLikeApplicationServiceImpl implements PictureLikeApplicationService {


    @Resource
    private PictureLikeDomainService pictureLikeDomainService;

    /**
     * 点赞图片
     *
     * @param pictureId 图片id
     * @param loginUser 登录用户
     * @return 是否点赞成功
     */
    @Override
    public Boolean likePicture(Long pictureId, User loginUser) {
        return pictureLikeDomainService.likePicture(pictureId, loginUser);

    }

    /**
     * 获取图片点赞数
     *
     * @param pictureId 图片的id
     * @return 点赞数
     */
    @Override
    public Long countByPictureId(Long pictureId) {
        return pictureLikeDomainService.countByPictureId(pictureId);
    }

    /**
     * 查询用户是否点过某个图片的赞
     *
     * @param userId    用户id
     * @param pictureId 图片id
     * @return 是否点过赞
     */
    @Override
    public boolean hasUserLiked(Long userId, Long pictureId) {
        return pictureLikeDomainService.hasUserLiked(userId, pictureId);
    }


}




