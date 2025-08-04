package com.lushihao.picturebackend.api.imagesearch.model;

import lombok.Data;

/**
 * 图片搜索结果
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-03   21:57
 */
@Data
public class ImageSearchResult {
    /**
     * 缩略图地址
     */
    private String thubmUrl;
    /**
     * 来源地址
     */
    private String fromUrl;
}
