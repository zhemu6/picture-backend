package com.lushihao.picturebackend.model.vo.space.analye;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 空间使用情况分析响应类
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-05   9:15
 */

@Data
public class SpaceUsageAnalyzeResponse implements Serializable {


    private static final long serialVersionUID = -8167103348055452913L;

    /**
     * 已使用空间大小
     */
    private Long usedSize;

    /**
     * 总空间大小
     */
    private Long maxSize;

    /**
     * 已使用空间大小比例
     */
    private Double sizeUsageRatio;

    /**
     * 已使用条数大小
     */
    private Long usedCount;

    /**
     * 总条数
     */
    private Long maxCount;

    /**
     * 已使用条数比例
     */
    private Double countUsageRatio;

}
