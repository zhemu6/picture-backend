package com.lushihao.picture.interfaces.dto.picture;

import com.lushihao.picture.infrastructure.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论分页查询条件
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-04   21:59
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PictureCommentQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1066577619235360385L;
    /**
     * 主键ID
     */
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
     * 创建时间
     */
    private Date createTime;

}
