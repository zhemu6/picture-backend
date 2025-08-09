package com.lushihao.picture.interfaces.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 图片更新请求
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-30   10:01
 */
@Data
public class PictureUpdateRequest implements Serializable {

    private static final long serialVersionUID = 6475853193384598838L;
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
