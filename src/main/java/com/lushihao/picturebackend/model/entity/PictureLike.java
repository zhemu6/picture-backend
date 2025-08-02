package com.lushihao.picturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 图片点赞表
 * @author lushihao
 * @TableName picture_like
 */
@TableName(value ="picture_like")
@Data
public class PictureLike implements Serializable {

    private static final long serialVersionUID = 1862757457127903638L;
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 点赞用户ID
     */
    private Long userId;

    /**
     * 被点赞的图片ID
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