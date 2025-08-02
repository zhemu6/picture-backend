package com.lushihao.picturebackend.model.vo;

import cn.hutool.json.JSONUtil;
import com.lushihao.picturebackend.model.entity.Picture;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.io.Serializable;
import java.util.List;

/**
 * 用于返回返回给前端的视图响应类
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-30   10:03
 */
@Data
public class PictureVO implements Serializable {

    private static final long serialVersionUID = 2549943850680259025L;

    /**
     * id
     */
    private Long id;

    /**
     * 图片 url
     */
    private String url;

    /**
     * 缩略图url地址
     */
    private String thumbnailUrl;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 分类
     */
    private String category;

    /**
     * 文件体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 相机型号
     */
    private String cameraModel;

    /**
     * 镜头型号
     */
    private String lensModel;

    /**
     * 光圈值
     */
    private Double fNumber;

    /**
     * ISO
     */
    private Integer iso;

    /**
     * 快门时间
     */
    private String exposureTime;

    /**
     * 焦距
     */
    private Double focalLength;

    /**
     * 拍摄时间（EXIF）
     */
    private Date takenTime;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建用户信息
     */
    private UserVO user;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 当前用户是否点过赞
     */
    private boolean hasLiked;

    /**
     * 收藏数
     */
    private Long favoriteCount;

    /**
     * 当前用户是否点收藏
     */
    private boolean hasFavorite;

    /**
     * 封装类转对象
     */
    public static Picture voToObj(PictureVO pictureVO) {
        if (pictureVO == null) {
            return null;
        }
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureVO, picture);
        // 类型不同，需要转换
        picture.setTags(JSONUtil.toJsonStr(pictureVO.getTags()));
        return picture;
    }

    /**
     * 对象转封装类
     */
    public static PictureVO objToVo(Picture picture) {
        if (picture == null) {
            return null;
        }
        PictureVO pictureVO = new PictureVO();
        BeanUtils.copyProperties(picture, pictureVO);
        // 类型不同，需要转换
        pictureVO.setTags(JSONUtil.toList(picture.getTags(), String.class));
        return pictureVO;
    }

}
