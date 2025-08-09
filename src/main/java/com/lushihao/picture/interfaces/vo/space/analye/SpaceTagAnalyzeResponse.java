package com.lushihao.picture.interfaces.vo.space.analye;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 空间标签分析响应类 这里只需要返回数量和使用次数即可
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-05   9:15
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SpaceTagAnalyzeResponse implements Serializable {

    private static final long serialVersionUID = 5342627923281913395L;

    /**
     * 类别
     */
    private String tag;

    /**
     * 数量
     */
    private Long count;


}
