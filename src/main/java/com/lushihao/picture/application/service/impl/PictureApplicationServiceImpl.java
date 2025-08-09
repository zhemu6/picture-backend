package com.lushihao.picture.application.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.picture.application.service.PictureApplicationService;
import com.lushihao.picture.application.service.PictureFavoriteApplicationService;
import com.lushihao.picture.application.service.PictureLikeApplicationService;
import com.lushihao.picture.application.service.UserApplicationService;
import com.lushihao.picture.domain.picture.service.PictureDomainService;
import com.lushihao.picture.infrastructure.api.aliyunai.model.CreateTaskResponse;
import com.lushihao.picture.infrastructure.common.DeleteRequest;
import com.lushihao.picture.infrastructure.exception.ErrorCode;
import com.lushihao.picture.infrastructure.exception.ThrowUtils;
import com.lushihao.picture.interfaces.dto.picture.*;
import com.lushihao.picture.interfaces.vo.user.UserVO;
import com.lushihao.picture.infrastructure.manager.upload.FilePictureUpload;
import com.lushihao.picture.domain.picture.entity.Picture;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.interfaces.vo.picture.PictureVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lushihao
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-07-30 09:56:20
 */
@Service
@Slf4j
public class PictureApplicationServiceImpl implements PictureApplicationService {

    @Resource
    private PictureDomainService pictureDomainService;
    @Resource
    private UserApplicationService userApplicationService;
    @Resource
    private PictureLikeApplicationService pictureLikeApplicationService;
    @Resource
    private PictureFavoriteApplicationService pictureFavoriteApplicationService;
    @Resource
    private FilePictureUpload filePictureUpload;


    /**
     * 上传图片
     *
     * @param inputSource          文件
     * @param pictureUploadRequest 前端发来的请求
     * @param loginUser            登录用户
     * @return PictureVO
     */
    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        return pictureDomainService.uploadPicture(inputSource, pictureUploadRequest, loginUser);
    }

    /**
     * 上传头像
     *
     * @param fileObj   头像文件
     * @param loginUser 登录用户
     * @return PictureVO
     */
    @Override
    public String uploadAvatar(MultipartFile fileObj, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        ThrowUtils.throwIf(fileObj == null, ErrorCode.NO_AUTH_ERROR, "上传文件格式错误");
        String uploadPathPrefix = String.format("public/%s/avatar", loginUser.getId());
        return filePictureUpload.uploadAvatar(fileObj, uploadPathPrefix);
    }


    /**
     * 获取查询条件 根据用户传入的参数 构造SQL查询
     *
     * @param pictureQueryRequest 前端传入查询的参数
     * @return 查询到的对象
     */
    @Override
    public LambdaQueryWrapper<Picture> getLambdaQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        return pictureDomainService.getLambdaQueryWrapper(pictureQueryRequest);
    }

    /**
     * 获取图片封装的方法 为原有的图片关联创建用户的信息
     *
     * @param picture 图片
     * @param request 请求
     * @return 封装类PictureVO
     */
    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        // 对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        User loginUser = userApplicationService.getLoginUser(request);
        // 获取图片id和登录用户id
        Long pictureId = picture.getId();
        Long loginUserId = loginUser.getId();
        // 获取图片点赞数和收藏数
        Long pictureLikeCount = this.getPictureLikeCount(pictureId);
        Long pictureFavoriteCount = this.getPictureFavoriteCount(pictureId);
        pictureVO.setLikeCount(pictureLikeCount);
        pictureVO.setFavoriteCount(pictureFavoriteCount);
        pictureVO.setHasLiked(pictureLikeApplicationService.hasUserLiked(loginUserId, pictureId));
        pictureVO.setHasFavorite(pictureFavoriteApplicationService.hasUserFavorite(loginUserId, pictureId));
        // 关联查询用户信息id
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userApplicationService.getUserById(userId);
            UserVO userVO = userApplicationService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    /**
     * 分页获取图片封装类
     *
     * @param picturePage 分页对象
     * @param request     请求
     * @return 分页封装对象
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        // 1.关联查询用户信息
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userApplicationService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        // 2.填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userApplicationService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        picture.validPicture();
    }


    /**
     * 图片审核功能
     *
     * @param pictureReviewRequest 图片审核请求类
     * @param loginUser            登录用户
     */
    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        pictureDomainService.doPictureReview(pictureReviewRequest, loginUser);
    }

    /**
     * 填充审核相关参数 如果是管理员传的图片 直接过审 用户的设置为待审核
     *
     * @param picture   图片
     * @param loginUser 登录用户
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        pictureDomainService.fillReviewParams(picture, loginUser);
    }

    /**
     * 批量抓去图片并上传
     *
     * @param pictureUploadByBatchRequest 批量抓去请求参数
     * @param loginUser                   登陆用户
     * @return 上传的图片数量
     */
    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        return pictureDomainService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
    }

    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {
        pictureDomainService.clearPictureFile(oldPicture);
    }


    /**
     * 校验空间权限
     *
     * @param loginUser 获得用户
     * @param picture   获取图片
     */
    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        pictureDomainService.checkPictureAuth(loginUser, picture);
    }

    /**
     * 删除 图片
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @Override
    public void deletePicture(DeleteRequest deleteRequest, HttpServletRequest request) {
        pictureDomainService.deletePicture(deleteRequest, request);
    }

    @Override
    public void editPicture(Picture picture, List<String> tags, User loginUser) {
        pictureDomainService.editPicture(picture, tags, loginUser);
    }

    /**
     * 根据颜色搜索图片
     *
     * @param spaceId   空间id
     * @param picColor  图片颜色 16
     * @param loginUser 用户获取登录用户
     * @return 按照相似度 返回一个PictureVO列表
     */
    @Override
    public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
        return pictureDomainService.searchPictureByColor(spaceId, picColor, loginUser);
    }

    /**
     * 批量编辑图片
     *
     * @param editRequest 编辑请求
     * @param loginUser   请求
     */
    @Override
    public void editPictureByBatch(PictureEditByBatchRequest editRequest, User loginUser) {
        pictureDomainService.editPictureByBatch(editRequest, loginUser);
    }


    /**
     * 创建AI拓图的工具类
     *
     * @param createPictureOutPaintingTaskRequest 拓图请求
     * @param request                             获取用户信息
     * @return CreateOutPaintingTaskResponse对象
     */
    @Override
    public CreateTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, HttpServletRequest request) {
        return pictureDomainService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest, request);
    }

    /**
     * 创建AI风格化的工具类
     *
     * @param createPictureCommonSynthesisTaskRequest 风格化请求
     * @param request                                 获取用户信息
     * @return CreateTaskResponse对象
     */
    @Override
    public CreateTaskResponse createPictureCommonSynthesisTask(CreatePictureCommonSynthesisTaskRequest createPictureCommonSynthesisTaskRequest, HttpServletRequest request) {
        return pictureDomainService.createPictureCommonSynthesisTask(createPictureCommonSynthesisTaskRequest, request);
    }

    @Override
    public Page<Picture> page(Page<Picture> picturePage, LambdaQueryWrapper<Picture> lambdaQueryWrapper) {
        return pictureDomainService.page(picturePage, lambdaQueryWrapper);
    }

    @Override
    public Picture getById(long id) {
        return pictureDomainService.getById(id);
    }

    @Override
    public boolean updateById(Picture picture) {
        return pictureDomainService.updateById(picture);
    }

    @Override
    public List<Object> selectPictureSizes(QueryWrapper<Picture> queryWrapper) {
        return pictureDomainService.selectPictureSizes(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getCategoryStatistics(QueryWrapper<Picture> queryWrapper) {
        return pictureDomainService.getCategoryStatistics(queryWrapper);
    }

    @Override
    public List<String> selectTageJsonList(QueryWrapper<Picture> queryWrapper) {
        return pictureDomainService.selectTageJsonList(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> selectTime(QueryWrapper<Picture> queryWrapper) {
        return pictureDomainService.selectTime(queryWrapper);
    }

    @Override
    public List<Long> getPictureSizes(QueryWrapper<Picture> queryWrapper) {
        return pictureDomainService.getPictureSizes(queryWrapper);
    }

    @Override
    public Picture getPictureByPictureId(Long pictureId) {
        return pictureDomainService.getPictureByPictureId(pictureId);
    }


    @Override
    public Long getPictureLikeCount(Long pictureId) {
        return pictureLikeApplicationService.countByPictureId(pictureId);
    }

    @Override
    public Long getPictureFavoriteCount(Long pictureId) {
        return pictureFavoriteApplicationService.countByPictureId(pictureId);
    }

}




