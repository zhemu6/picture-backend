package com.lushihao.picturebackend.model.dto.pictureLike;

import lombok.Data;

/**
 * 图片点赞请求
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-01   20:40
 */
@Data
public class PictureLikeRequest {
    /**
     * 点赞的图片id
     */
    private Long  pictureId;

}
