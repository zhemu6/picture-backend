package com.lushihao.picturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 空间枚举类别
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-02   20:44
 */
@Getter
public enum SpaceLevelEnum {
    /**
     * 定义空间包含哪些等级
     */
    // 分别是0.1G、1G和10G
    COMMON("普通版", 0, 100, 100L * 1024 * 1024),
    PROFESSIONAL("专业版", 1, 1000, 1000L * 1024 * 1024),
    FLAGSHIP("旗舰版", 2, 10000, 10000L * 1024 * 1024);

    private final String text;
    private final int value;
    private final long maxCount;
    private final long maxSize;

    // 构造器
    SpaceLevelEnum(String text, int value, long maxCount, long maxSize) {
        this.text = text;
        this.value = value;
        this.maxCount = maxCount;
        this.maxSize = maxSize;
    }

    /**
     * 通过value值获得文本 比如通过 "0" 获得 "普通版"
     *
     * @param value
     * @return
     */
    public static SpaceLevelEnum getEnumByValue(int value) {
        // 首先判断这个value是否为空
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        // 遍历枚举类
        for (SpaceLevelEnum anEnum : SpaceLevelEnum.values()) {
            if (anEnum.value == (value)) {
                return anEnum;
            }
        }
        return null;
    }


}
