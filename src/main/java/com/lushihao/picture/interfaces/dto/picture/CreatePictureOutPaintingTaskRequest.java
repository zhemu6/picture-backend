package com.lushihao.picture.interfaces.dto.picture;

import com.lushihao.picture.infrastructure.api.aliyunai.model.CreateOutPaintingTaskRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建拓图任务请求封装类
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-04   16:00
 */
@Data
public class CreatePictureOutPaintingTaskRequest implements Serializable {


    private static final long serialVersionUID = -7505299323329949719L;

    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 扩图参数
     */
    private CreateOutPaintingTaskRequest.Parameters parameters;


}
