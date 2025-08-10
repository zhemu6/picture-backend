package com.lushihao.picture.interfaces.vo.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-10   17:59
 */
@Data
public class PictureRankVO implements Serializable {


    private static final long serialVersionUID = -7897644663187419746L;
    /**
     * 图片ID
     */
    private Long pictureId;
    /**
     * 点赞数
     */
    private Long likeCount;
    /**
     * 图片 url
     */
    private String url;
    /**
     * 缩略图url地址
     */
    private String thumbnailUrl;
    /**
     * 简介
     */
    private String introduction;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 标签
     */
    private List<String> tags;
    /**
     * 分类
     */
    private String category;
    /**
     * 日/周/月
     */
    private Integer rankNum;
    /**
     * 日/周/月
     */
    private String type;
}
