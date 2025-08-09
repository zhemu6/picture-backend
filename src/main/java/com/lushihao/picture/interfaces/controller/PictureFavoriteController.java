package com.lushihao.picture.interfaces.controller;

import com.lushihao.picture.infrastructure.common.BaseResponse;
import com.lushihao.picture.infrastructure.common.ResultUtils;
import com.lushihao.picture.infrastructure.exception.ErrorCode;
import com.lushihao.picture.infrastructure.exception.ThrowUtils;
import com.lushihao.picture.interfaces.dto.picture.PictureFavoriteRequest;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.application.service.PictureFavoriteApplicationService;
import com.lushihao.picture.application.service.PictureApplicationService;
import com.lushihao.picture.application.service.UserApplicationService;
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
    private PictureFavoriteApplicationService pictureFavoriteApplicationService;

    @Resource
    private PictureApplicationService pictureApplicationService;

    @Resource
    private UserApplicationService userApplicationService;

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
        User loginUser = userApplicationService.getLoginUser(request);
        // 参数校验
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR,"请登录后重试");
        Boolean isFavorite = pictureFavoriteApplicationService.favoritePicture(pictureId, loginUser);
        ThrowUtils.throwIf(!isFavorite, ErrorCode.OPERATION_ERROR,"收藏失败，请稍后重试");
        return ResultUtils.success(true);
    }

}
