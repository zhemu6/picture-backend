package com.lushihao.picture.interfaces.controller;

import com.lushihao.picture.infrastructure.common.BaseResponse;
import com.lushihao.picture.infrastructure.common.ResultUtils;
import com.lushihao.picture.infrastructure.exception.ErrorCode;
import com.lushihao.picture.infrastructure.exception.ThrowUtils;
import com.lushihao.picture.interfaces.dto.picture.PictureLikeRequest;
import com.lushihao.picture.domain.picture.entity.Picture;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.application.service.PictureLikeApplicationService;
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
 * 图片点赞控制层
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-01   20:36
 */

@Slf4j
@RestController
@RequestMapping("/pictureLike")
public class PictureLikeController {

    @Resource
    private PictureLikeApplicationService pictureLikeApplicationService;

    @Resource
    private PictureApplicationService pictureApplicationService;

    @Resource
    private UserApplicationService userApplicationService;

    /**
     * 点赞图片
     * @param pictureLikeRequest 图片点赞请求
     * @param request 请求 用于获取登录用户
     * @return 是否点赞成功
     */
    @PostMapping("/like")
    public BaseResponse<Boolean> likePicture(@RequestBody PictureLikeRequest pictureLikeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureLikeRequest == null || pictureLikeRequest.getPictureId() <= 0, ErrorCode.PARAMS_ERROR);
        // 获取登录用户和图片id
        Long pictureId = pictureLikeRequest.getPictureId();
        User loginUser = userApplicationService.getLoginUser(request);
        // 参数校验
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR,"请登录后重试");
        // 还需要判断这个图片是否存在
        Picture picture = pictureApplicationService.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "对不起，您点赞的图片不存在");
        Boolean isLike = pictureLikeApplicationService.likePicture(pictureId, loginUser);
        // 点赞失败
        ThrowUtils.throwIf(!isLike, ErrorCode.OPERATION_ERROR,"点赞失败，请稍后重试");
        return ResultUtils.success(true);
    }

}
