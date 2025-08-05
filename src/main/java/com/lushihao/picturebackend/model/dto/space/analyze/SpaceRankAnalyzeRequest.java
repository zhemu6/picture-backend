package com.lushihao.picturebackend.model.dto.space.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间排名分析请求 返回排名前10 的空间
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-05   11:07
 */
@Data
public class SpaceRankAnalyzeRequest implements Serializable {

    private static final long serialVersionUID = -8579835731055972075L;
    /**
     * 取前多少个
     */
    private Integer topK=10;

}
