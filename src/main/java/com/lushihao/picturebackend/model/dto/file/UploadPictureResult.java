package com.lushihao.picturebackend.model.dto.file;

import lombok.Data;

import java.util.Date;

/**
 * 接受图片解析信息的包装类(这些是上传图片后获得的)
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-30   10:10
 */
@Data
public class UploadPictureResult {
    /**
     * 图片地址
     */
    private String url;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 文件体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private int picWidth;

    /**
     * 图片高度
     */
    private int picHeight;

    /**
     * 图片宽高比
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
}
