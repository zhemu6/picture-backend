package com.lushihao.picturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 图片上传请求 用户上传图片可以重复上传 实际上会保存到数据库中获得id
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-30   10:01
 */
@Data
public class PictureUploadRequest  implements Serializable {
    private static final long serialVersionUID = -4738311912109569403L;
    /*
     * 图片的id 用于修改
     */
    private Long  id;

    /*
     * 图片的URL地址
     */
    private String fileUrl;

    /*
     * 图片的名称
     */
    private String fileName;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private String tags;

}
