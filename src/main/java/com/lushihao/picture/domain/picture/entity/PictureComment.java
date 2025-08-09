package com.lushihao.picture.domain.picture.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 图片评论表（支持楼中楼）
 * @author lushihao
 * @TableName picture_comment
 */
@TableName(value ="picture_comment")
@Data
public class PictureComment implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 评论用户ID
     */
    private Long userId;

    /**
     * 被评论的图片ID
     */
    private Long pictureId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论ID（0表示一级评论）
     */
    private Long parentId;

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
    @TableLogic
    private Integer isDelete;
}