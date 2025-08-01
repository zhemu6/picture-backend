package com.lushihao.picturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 图片审核状态枚举
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-31   10:46
 */
@Getter
public enum PictureReviewsStatusEnum {

    Unreviewed("未审核", 0),
    Pass("通过", 1),
    Reject("拒绝", 2);


    private final String text;

    private final int value;

    // 构造器
    PictureReviewsStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 通过value值获得文本 比如通过 "user" 获得 "用户"
     *
     * @param value
     * @return
     */
    public static PictureReviewsStatusEnum getEnumByValue(Integer value) {
        // 首先判断这个value是否为空
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        // 遍历枚举类
        for (PictureReviewsStatusEnum anEnum : PictureReviewsStatusEnum.values()) {
            if (anEnum.value == value) {
                return anEnum;
            }
        }
        return null;
    }
}
