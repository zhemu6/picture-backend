package com.lushihao.picturebackend.model.dto.picture;

import com.lushihao.picturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-30   12:32
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PictureQueryRequest extends PageRequest implements Serializable {


    private static final long serialVersionUID = 5276485175274472982L;

    /**
     * id
     */
    private Long id;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

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
     * 搜索词（同时搜名称、简介等）
     */
    private String searchText;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核人 id
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private Date reviewTime;


}
