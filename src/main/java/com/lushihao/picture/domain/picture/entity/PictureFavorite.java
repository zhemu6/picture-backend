package com.lushihao.picture.domain.picture.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import lombok.Data;

/**
 * 图片收藏表
 * @author lushihao
 * @TableName picture_favorite
 */
@TableName(value ="picture_favorite")
@Data
public class PictureFavorite {


    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 收藏用户ID
     */
    private Long userId;

    /**
     * 被收藏的图片ID
     */
    private Long pictureId;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;
}