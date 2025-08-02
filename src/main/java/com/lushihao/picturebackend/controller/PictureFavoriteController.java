package com.lushihao.picturebackend.controller;

import com.lushihao.picturebackend.common.BaseResponse;
import com.lushihao.picturebackend.common.ResultUtils;
import com.lushihao.picturebackend.exception.ErrorCode;
import com.lushihao.picturebackend.exception.ThrowUtils;
import com.lushihao.picturebackend.model.dto.pictureFavorite.PictureFavoriteRequest;
import com.lushihao.picturebackend.model.dto.pictureLike.PictureLikeRequest;
import com.lushihao.picturebackend.model.entity.User;
import com.lushihao.picturebackend.service.PictureFavoriteService;
import com.lushihao.picturebackend.service.PictureService;
import com.lushihao.picturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-01   21:36
 */
@Slf4j
@RequestMapping("/pictureFavorite")
@RestController
public class PictureFavoriteController {
    @Resource
    private PictureFavoriteService pictureFavoriteService;

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    /**
     * 收藏图片
     * @param pictureFavoriteRequest 图片收藏请求
     * @param request 请求 用于获取登录用户
     * @return 是否收藏成功
     */
    @PostMapping("/favour")
    public BaseResponse<Boolean> favoritePicture(@RequestBody PictureFavoriteRequest pictureFavoriteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureFavoriteRequest == null || pictureFavoriteRequest.getPictureId() <= 0, ErrorCode.PARAMS_ERROR);
        // 获取登录用户和图片id
        Long pictureId = pictureFavoriteRequest.getPictureId();
        User loginUser = userService.getLoginUser(request);
        // 参数校验
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR,"请登录后重试");
        Boolean isFavorite = pictureFavoriteService.favoritePicture(pictureId, loginUser);
        ThrowUtils.throwIf(!isFavorite, ErrorCode.OPERATION_ERROR,"收藏失败，请稍后重试");
        return ResultUtils.success(true);
    }

}
