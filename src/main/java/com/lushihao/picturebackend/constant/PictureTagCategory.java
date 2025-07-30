package com.lushihao.picturebackend.constant;

import lombok.Data;

import java.util.List;

/**
 * 图片标签和分类响应对象
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-30   15:09
 */
@Data
public class PictureTagCategory {
    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 分类列表
     */
    private List<String> categoryList;
}
