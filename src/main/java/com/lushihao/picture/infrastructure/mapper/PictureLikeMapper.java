package com.lushihao.picture.infrastructure.mapper;

import com.lushihao.picture.domain.picture.entity.PictureLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author lushihao
 * @description 针对表【picture_like(图片点赞表)】的数据库操作Mapper
 * @createDate 2025-08-01 20:27:05
 * @Entity com.lushihao.picture.domain.picture.entity.PictureLike
 */
public interface PictureLikeMapper extends BaseMapper<PictureLike> {
    @Select("  SELECT COUNT(*) FROM picture_like pl INNER JOIN picture p ON pl.picture_id = p.id WHERE p.user_id = #{userId} AND pl.is_delete = 0 AND p.is_delete = 0 AND p.space_id IS NULL")
    Long countLikesByUserId(Long userId);
    @Select("SELECT COUNT(*) \n" +
            "    FROM picture\n" +
            "    WHERE user_id = #{userId}\n" +
            "      AND is_delete = 0\n" +
            "      AND space_id IS NULL")
    Long countUploadsByUserId(Long userId);
}




