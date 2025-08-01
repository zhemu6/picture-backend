package com.lushihao.picturebackend.model.dto.file;

import lombok.Data;

import java.util.Date;

/**
 * 接受图片解析信息的包装类(这些是上传图片后获得的)
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-30   10:10
 */
@Data
public class UploadAvatarResult {
    /**
     * 图片地址
     */
    private String url;

}
