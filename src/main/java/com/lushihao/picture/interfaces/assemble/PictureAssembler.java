package com.lushihao.picture.interfaces.assemble;

import cn.hutool.core.bean.BeanUtil;
import com.lushihao.picture.domain.picture.entity.Picture;
import com.lushihao.picture.interfaces.dto.picture.PictureEditRequest;
import com.lushihao.picture.interfaces.dto.picture.PictureUpdateRequest;

/**
 * 图片类别对象转换
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-08   20:02
 */
public class PictureAssembler {
    /**
     * 图片添加请求转换成实体类
     *
     * @param pictureEditRequest 图片添加请求
     * @return 图片实体类
     */
    public static Picture toPictureEntity(PictureEditRequest pictureEditRequest) {
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureEditRequest, picture);
        return picture;
    }

    /**
     * 图片更新请求转换成实体类
     *
     * @param pictureUpdateRequest 图片更新请求
     * @return 图片实体类
     */
    public static Picture toPictureEntity(PictureUpdateRequest pictureUpdateRequest) {
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUpdateRequest, picture);
        return picture;
    }
}
