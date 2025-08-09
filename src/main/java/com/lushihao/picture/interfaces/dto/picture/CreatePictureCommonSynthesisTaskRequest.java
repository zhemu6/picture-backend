package com.lushihao.picture.interfaces.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建图片风格化任务请求封装类
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-04   16:00
 */
@Data
public class CreatePictureCommonSynthesisTaskRequest implements Serializable {


    private static final long serialVersionUID = 4049586620829082356L;
    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 风格提示词
     */
    private String prompt;


}
