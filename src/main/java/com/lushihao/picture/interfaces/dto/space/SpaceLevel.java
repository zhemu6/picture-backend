package com.lushihao.picture.interfaces.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 空间级别
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-03   9:47
 */
@Data
@AllArgsConstructor
public class SpaceLevel {
    /**
     * 空间的级别id 0 1 2
     */
    private int value;
    /**
     * 级别名称
     */
    private String text;
    /**
     * 最大条数
     */
    private long maxCount;
    /**
     * 最大容量
     */
    private long maxSize;

}
