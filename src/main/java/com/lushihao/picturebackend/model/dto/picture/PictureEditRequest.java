package com.lushihao.picturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 图片编辑请求 一般情况下给普通用户使用，可修改的字段范围小于更新请求：
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-30   10:01
 */
@Data
public class PictureEditRequest implements Serializable {

    private static final long serialVersionUID = -8175654098956136432L;
    /**
     * 图片的id 用于修改
     */
    private Long  id;

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

}
