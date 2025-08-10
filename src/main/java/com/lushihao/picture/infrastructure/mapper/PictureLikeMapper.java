package com.lushihao.picture.infrastructure.mapper;

import com.lushihao.picture.domain.picture.entity.PictureLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lushihao.picture.interfaces.dto.picture.PictureRankCount;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;


import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * 统计一定时间内数据的点赞数
     */
    @Select("        SELECT pl.picture_id, COUNT(1) AS like_count\n" +
            "        FROM picture_like pl\n" +
            "        INNER JOIN picture p ON pl.picture_id = p.id\n" +
            "        WHERE pl.is_delete = 0\n" +
            "          AND p.is_delete = 0\n" +
            "          AND p.space_id IS NULL\n" +
            "          AND pl.create_time BETWEEN #{startTime} AND #{endTime}\n" +
            "        GROUP BY pl.picture_id\n" +
            "        ORDER BY like_count DESC\n" +
            "        LIMIT #{limit}")
    List<PictureRankCount> getTopLikedPictures(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime,
                                               @Param("limit") int limit);
}




