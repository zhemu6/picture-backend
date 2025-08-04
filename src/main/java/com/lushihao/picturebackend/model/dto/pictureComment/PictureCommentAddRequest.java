package com.lushihao.picturebackend.model.dto.pictureComment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户对图片的评论添加请求
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-04   21:04
 */
@Data
public class PictureCommentAddRequest implements Serializable {

    private static final long serialVersionUID = -12876429186726421L;


    /**
     * 被评论的图片ID
     */
    private Long pictureId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论ID（0表示一级评论） 默认一级评论
     */
    private Long parentId = 0L;


}
