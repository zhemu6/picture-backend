package com.lushihao.picturebackend.model.vo.space.analye;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文件大小分析响应类 这里只需要返回不同区间和对应的数量
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-05   9:15
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SpaceSizeAnalyzeResponse implements Serializable {

    private static final long serialVersionUID = 2908675177110251554L;
    /**
     * 图片大小范围
     */
    private String sizeRange;

    /**
     * 数量
     */
    private Long count;


}
