package com.lushihao.picturebackend.model.vo.space.analye;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 空间类别统计分析响应类
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-05   9:15
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SpaceCategoryAnalyzeResponse implements Serializable {

    private static final long serialVersionUID = -1836757024300872744L;

    /**
     * 类别
     */
    private String category;

    /**
     * 数量
     */
    private Long count;

    /**
     * 当前分类图片中 总大小
     */
    private Long totalSize;

}
