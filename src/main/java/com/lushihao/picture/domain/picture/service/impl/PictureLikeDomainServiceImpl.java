package com.lushihao.picture.domain.picture.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lushihao.picture.application.service.PictureLikeApplicationService;
import com.lushihao.picture.domain.picture.entity.PictureLike;
import com.lushihao.picture.domain.picture.repository.PictureLikeRepository;
import com.lushihao.picture.domain.picture.service.PictureLikeDomainService;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.infrastructure.exception.ErrorCode;
import com.lushihao.picture.infrastructure.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author lushihao
 * @description 针对表【picture_like(图片点赞表)】的数据库操作Service实现
 * @createDate 2025-08-01 20:27:05
 */
@Service
public class PictureLikeDomainServiceImpl implements PictureLikeDomainService {

    @Resource
    private PictureLikeRepository pictureLikeRepository;

    /**
     * 点赞图片
     *
     * @param pictureId 图片id
     * @param loginUser 登录用户
     * @return 是否点赞成功
     */
    @Override
    public Boolean likePicture(Long pictureId, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 获取用户id和图片id
        Long userId = loginUser.getId();
        // 查询用户是否已经点赞
        LambdaQueryWrapper<PictureLike> pictureLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        pictureLikeLambdaQueryWrapper.eq(PictureLike::getUserId, userId).eq(PictureLike::getPictureId, pictureId);
        PictureLike existing = pictureLikeRepository.getOne(pictureLikeLambdaQueryWrapper);
        // 如果查询到
        if (existing != null) {
            if (existing.getIsDelete() == 0) {
                // 已收藏 -> 取消收藏
                existing.setIsDelete(1);
            } else {
                // 曾经取消收藏 -> 再次收藏
                existing.setIsDelete(0);
            }
            return pictureLikeRepository.updateById(existing);
        }
        // 未点赞 -> 点赞 insert为1 代表已点赞
        PictureLike pictureLike = new PictureLike();
        pictureLike.setUserId(userId);
        pictureLike.setPictureId(pictureId);
        return pictureLikeRepository.save(pictureLike);
    }

    /**
     * 获取图片点赞数
     *
     * @param pictureId 图片的id
     * @return 点赞数
     */
    @Override
    public Long countByPictureId(Long pictureId) {
        return pictureLikeRepository.lambdaQuery().eq(PictureLike::getPictureId, pictureId).eq(PictureLike::getIsDelete, 0).count();
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
        return pictureLikeRepository.lambdaQuery()
                .eq(PictureLike::getUserId, userId)
                .eq(PictureLike::getPictureId, pictureId)
                .eq(PictureLike::getIsDelete, 0)
                .exists(); // 或 count() > 0
    }


}




