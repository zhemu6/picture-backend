package com.lushihao.picturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.picturebackend.common.DeleteRequest;
import com.lushihao.picturebackend.model.dto.picture.*;
import com.lushihao.picturebackend.model.dto.user.UserLoginRequest;
import com.lushihao.picturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lushihao.picturebackend.model.entity.User;
import com.lushihao.picturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;

/**
* @author lushihao
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-07-30 09:56:20
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     * @param inputSource 文件输入源（本地图片或url）
     * @param pictureUploadRequest 前端发来的请求
     * @param loginUser 登录用户
     * @return PictureVO
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);


    String uploadAvatar(MultipartFile multipartFile, User loginUser);

    /**
     * 获取查询条件 根据用户传入的参数 构造SQL查询
     * @param pictureQueryRequest 前端传入查询的参数
     * @return 查询到的对象
     */
    LambdaQueryWrapper<Picture> getLambdaQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片封装的方法 为原有的图片关联创建用户的信息
     * @param picture 图片
     * @param request 请求
     * @return 封装类PictureVO
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 分页获取图片封装类
     * @param picturePage 分页对象
     * @param request 请求
     * @return 分页封装对象
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage,HttpServletRequest request);

    /**
     * 图片校验功能
     * @param picture 传入图片
     */
    void validPicture(Picture picture);

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
     * @param pictureId 图片的id
     * @return 点赞数
     */
    Long getPictureLikeCount(Long pictureId);

    Long getPictureFavoriteCount(Long pictureId);

    /**
     * 校验空间权限
     * @param loginUser 获得用户
     * @param picture 获取图片
     */
    void checkPictureAuth(User loginUser,Picture picture);

    /**
     * 删除 图片
     * @param deleteRequest
     * @param request
     * @return
     */
    void deletePicture(DeleteRequest deleteRequest, HttpServletRequest request);


    void   editPicture(PictureEditRequest editRequest, HttpServletRequest request);

    /**
     * 根据颜色搜索图片
     *
     * @param spaceId  空间id
     * @param picColor 图片颜色 16
     * @param request  用户获取登录用户
     * @return 按照相似度 返回一个PictureVO列表
     */
    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, HttpServletRequest request);

    /**
     * 批量编辑图片
     *
     * @param editRequest 编辑请求
     * @param request     请求
     */
    void editPictureByBatch(PictureEditByBatchRequest editRequest, HttpServletRequest request);

}
