package com.lushihao.picture.interfaces.vo.space.analye;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户行为分析响应类
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-05   9:15
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SpaceUserAnalyzeResponse implements Serializable {


    private static final long serialVersionUID = -9210296019293896679L;
    /**
     * 时间区间
     */
    private String period;

    /**
     * 上传数量
     */
    private Long count;

}
