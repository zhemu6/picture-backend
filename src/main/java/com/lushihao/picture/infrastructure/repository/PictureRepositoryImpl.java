package com.lushihao.picture.infrastructure.repository;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.picture.domain.picture.entity.Picture;
import com.lushihao.picture.domain.picture.repository.PictureRepository;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.domain.user.repository.UserRepository;
import com.lushihao.picture.infrastructure.mapper.PictureMapper;
import com.lushihao.picture.infrastructure.mapper.UserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 图片领域仓储的具体实现
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-08   19:10
 */
@Service
public class PictureRepositoryImpl extends ServiceImpl<PictureMapper, Picture> implements PictureRepository {

    @Resource
    private PictureMapper pictureMapper;

    @Override
    public List<Object> selectPictureSizes(QueryWrapper<Picture> queryWrapper) {
        return pictureMapper.selectObjs(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> selectCategoryStatistics(QueryWrapper<Picture> queryWrapper) {
        return pictureMapper.selectMaps(queryWrapper);
    }

    @Override
    public List<String> selectTageJsonList(QueryWrapper<Picture> queryWrapper) {
        return pictureMapper.selectObjs(queryWrapper).stream()
                .filter(ObjUtil::isNotNull)
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> selectTime(QueryWrapper<Picture> queryWrapper) {
        return pictureMapper.selectMaps(queryWrapper);
    }

    @Override
    public List<Long> getPictureSizes(QueryWrapper<Picture> queryWrapper) {
        return pictureMapper.selectObjs(queryWrapper)
                .stream()
                .map(size -> ((Number) size).longValue())
                .collect(Collectors.toList());
    }
}
