package com.lushihao.picturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.picturebackend.model.dto.picture.PictureQueryRequest;
import com.lushihao.picturebackend.model.dto.picture.PictureUploadRequest;
import com.lushihao.picturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lushihao.picturebackend.model.entity.User;
import com.lushihao.picturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author lushihao
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-07-30 09:56:20
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     * @param multipartFile 文件
     * @param pictureUploadRequest 前端发来的请求
     * @param loginUser 登录用户
     * @return PictureVO
     */
    PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser);

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

}
