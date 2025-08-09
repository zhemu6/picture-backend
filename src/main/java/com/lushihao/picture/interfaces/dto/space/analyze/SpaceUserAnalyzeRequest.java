package com.lushihao.picture.interfaces.dto.space.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户行为分析请求封装类 用户上传行为分析需要增加时间维度（日、周、月）和用户 ID参数，支持只分析某个用户上传图片的情况。
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-05   9:53
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceUserAnalyzeRequest extends SpaceAnalyzeRequest{

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 时间维度 day / week / month
     */
    private String timeDimension;
}
