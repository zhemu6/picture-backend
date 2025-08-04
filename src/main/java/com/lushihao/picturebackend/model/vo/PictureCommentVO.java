package com.lushihao.picturebackend.model.vo;

import cn.hutool.json.JSONUtil;
import com.lushihao.picturebackend.model.entity.Picture;
import com.lushihao.picturebackend.model.entity.PictureComment;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 视图对象类
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-04   21:09
 */
@Data
public class PictureCommentVO implements Serializable {


    private static final long serialVersionUID = 5942149731777913633L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 评论用户ID
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

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

    /**
     * 是否是本人评论（前端可判断是否展示删除按钮）
     */
    private Boolean isSelf;


    /**
     * 封装类转对象
     */
    public static PictureComment voToObj(PictureCommentVO pictureCommentVO) {
        if (pictureCommentVO == null) {
            return null;
        }
        PictureComment pictureComment = new PictureComment();
        BeanUtils.copyProperties(pictureCommentVO, pictureComment);
        return pictureComment;
    }

    /**
     * 对象转封装类
     */
    public static PictureCommentVO objToVo(PictureComment pictureComment) {
        if (pictureComment == null) {
            return null;
        }
        PictureCommentVO pictureCommentVO = new PictureCommentVO();
        BeanUtils.copyProperties(pictureComment, pictureCommentVO);
        return pictureCommentVO;
    }

}
