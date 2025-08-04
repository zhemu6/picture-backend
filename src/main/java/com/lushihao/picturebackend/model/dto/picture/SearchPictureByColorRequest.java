package com.lushihao.picturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 通过颜色搜索请求类
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-04   9:57
 */
@Data
public class SearchPictureByColorRequest implements Serializable {


    private static final long serialVersionUID = 4139634182199282104L;

    /**
     * 图片主色调
     */
    private String picColor;
    /**
     * 空间id
     */
    private Long spaceId;

}


