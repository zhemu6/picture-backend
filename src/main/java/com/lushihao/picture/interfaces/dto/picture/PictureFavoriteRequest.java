package com.lushihao.picture.interfaces.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片收藏
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-01   21:39
 */
@Data
public class PictureFavoriteRequest implements Serializable {

    private static final long serialVersionUID = 4496330476590383845L;
    /**
     * 图片id
     */
    private Long pictureId;

}
