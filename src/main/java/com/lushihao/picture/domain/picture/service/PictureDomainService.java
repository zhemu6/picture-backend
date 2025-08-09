package com.lushihao.picture.domain.picture.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lushihao.picture.domain.picture.entity.Picture;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.infrastructure.api.aliyunai.model.CreateTaskResponse;
import com.lushihao.picture.infrastructure.common.DeleteRequest;
import com.lushihao.picture.interfaces.dto.picture.*;
import com.lushihao.picture.interfaces.vo.picture.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author lushihao
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2025-07-30 09:56:20
 */
public interface PictureDomainService {

    /**
     * 上传图片
     *
     * @param inputSource          文件输入源（本地图片或url）
     * @param pictureUploadRequest 前端发来的请求
     * @param loginUser            登录用户
     * @return PictureVO
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);


    /**
     * 获取查询条件 根据用户传入的参数 构造SQL查询
     *
     * @param pictureQueryRequest 前端传入查询的参数
     * @return 查询到的对象
     */
    LambdaQueryWrapper<Picture> getLambdaQueryWrapper(PictureQueryRequest pictureQueryRequest);


    /**
     * 图片审核功能
     *
     * @param pictureReviewRequest 图片审核请求类
     * @param loginUser            登录用户
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充审核相关参数 如果是管理员传的图片 直接过审 用户的设置为待审核
     *
     * @param picture   图片
     * @param loginUser 登录用户
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓去图片并上传
     *
     * @param pictureUploadByBatchRequest 批量抓去请求参数
     * @param loginUser                   登陆用户
     * @return 上传的图片数量
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);

    //This method clears the picture file of the given Picture object
    void clearPictureFile(Picture oldPicture);

    /**
     * 获取图片点赞数
     *
     * @param pictureId 图片的id
     * @return 点赞数
     */
    Long getPictureLikeCount(Long pictureId);

    Long getPictureFavoriteCount(Long pictureId);

    /**
     * 校验空间权限
     *
     * @param loginUser 获得用户
     * @param picture   获取图片
     */
    void checkPictureAuth(User loginUser, Picture picture);

    /**
     * 删除 图片
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    void deletePicture(DeleteRequest deleteRequest, HttpServletRequest request);


    void editPicture(Picture picture, List<String> tags, User loginUser);

    /**
     * 根据颜色搜索图片
     *
     * @param spaceId   空间id
     * @param picColor  图片颜色 16
     * @param loginUser 用户获取登录用户
     * @return 按照相似度 返回一个PictureVO列表
     */
    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    /**
     * 批量编辑图片
     *
     * @param editRequest 编辑请求
     * @param loginUser   请求
     */
    void editPictureByBatch(PictureEditByBatchRequest editRequest, User loginUser);

    /**
     * 创建AI拓图的工具类
     *
     * @param createPictureOutPaintingTaskRequest 拓图请求
     * @param request                             获取用户信息
     * @return CreateTaskResponse对象
     */
    CreateTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, HttpServletRequest request);

    /**
     * 创建AI风格化的工具类
     *
     * @param createPictureCommonSynthesisTaskRequest 风格化请求
     * @param request                                 获取用户信息
     * @return CreateTaskResponse对象
     */
    CreateTaskResponse createPictureCommonSynthesisTask(CreatePictureCommonSynthesisTaskRequest createPictureCommonSynthesisTaskRequest, HttpServletRequest request);

    Page<Picture> page(Page<Picture> picturePage, LambdaQueryWrapper<Picture> lambdaQueryWrapper);

    Picture getById(long id);

    boolean updateById(Picture picture);

    List<Object> selectPictureSizes(QueryWrapper<Picture> queryWrapper);

    List<Map<String, Object>> getCategoryStatistics(QueryWrapper<Picture> queryWrapper);

    List<String> selectTageJsonList(QueryWrapper<Picture> queryWrapper);

    List<Map<String, Object>> selectTime(QueryWrapper<Picture> queryWrapper);

    List<Long> getPictureSizes(QueryWrapper<Picture> queryWrapper);

    Picture getPictureByPictureId(Long pictureId);

    Long getUserLikeCount(Long userId);

    Long getUserUploadCount(Long userId);
}
