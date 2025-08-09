package com.lushihao.picture.interfaces.dto.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 公共的图片分析请求类
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-05   8:42
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SpaceAnalyzeRequest implements Serializable {

    private static final long serialVersionUID = 1840053762960347439L;
    /**
     * 空间id 用户查询自己的空间
     */
    private Long spaceId;
    /**
     * 是否查询公共图库 管理员查询公共图库
     */
    private boolean queryPublic;
    /**
     * 全空间分析 管理员查询所有的空间
     */
    private boolean queryAll;

}
