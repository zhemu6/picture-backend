package com.lushihao.picturebackend.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 头像VO
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-01   19:09
 */
@Data
public class AvatarVO implements Serializable {

    private static final long serialVersionUID = 8876640873931413818L;

    /**
     * 图片地址
     */
    private String url;
}
