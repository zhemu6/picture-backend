package com.lushihao.picture.domain.picture.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lushihao.picture.domain.picture.entity.Picture;

import java.util.List;
import java.util.Map;

/**
 * 图片仓储
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-08   19:09
 */
public interface PictureRepository extends IService<Picture> {
    /**
     * 通过图片获取图片列表占用空间
     *
     * @param queryWrapper
     * @return
     */
    List<Object> selectPictureSizes(QueryWrapper<Picture> queryWrapper);


    /**
     * 查询分类统计
     *
     * @param queryWrapper
     * @return
     */
    List<Map<String, Object>> selectCategoryStatistics(QueryWrapper<Picture> queryWrapper);

    /**
     * 获取空间中图片的分标签统计情况
     */
    List<String> selectTageJsonList(QueryWrapper<Picture> queryWrapper);

    /**
     * 获取用户在时间维度上上传图片的统计情况
     *
     * @param queryWrapper
     * @return
     */
    List<Map<String, Object>> selectTime(QueryWrapper<Picture> queryWrapper);

    /**
     * 获取图片的尺寸分布情况
     *
     * @param queryWrapper
     * @return
     */
    List<Long> getPictureSizes(QueryWrapper<Picture> queryWrapper);

}
