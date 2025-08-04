package com.lushihao.picturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 图片批量编辑请求
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-04   12:13
 */
@Data
public class PictureEditByBatchRequest implements Serializable {

    private static final long serialVersionUID = 241873965469223657L;

    /**
     * 批量请求 图片id
     */
    private List<Long> pictureIds;

    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 命名规则
     */
    private String nameRule;
}
