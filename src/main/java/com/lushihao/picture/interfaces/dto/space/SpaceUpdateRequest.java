package com.lushihao.picture.interfaces.dto.space;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 空间更新请求 提供给管理员 可以允许修改级别和限额
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-02   20:35
 */
@Data
public class SpaceUpdateRequest implements Serializable {


    private static final long serialVersionUID = -7331593369573542007L;
    /**
     * id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大小
     */
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    private Long maxCount;

}
