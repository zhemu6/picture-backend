package com.lushihao.picture.domain.picture.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.Data;

/**
 * @author lushihao
 * @TableName picture_rank
 */
@TableName(value = "picture_rank")
@Data
public class PictureRank implements Serializable {

    private static final long serialVersionUID = -2566352500058770171L;
    /**
     *
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 榜单类型: day/week/month
     */
    private String type;

    /**
     * 图片ID
     */
    private Long pictureId;

    /**
     * 点赞数
     */
    private Long likeCount;
    /**
     * 排名
     */
    private Integer rankNum;
    /**
     * 统计开始时间
     */
    private LocalDateTime  startTime;

    /**
     * 统计结束时间
     */
    private LocalDateTime  endTime;

    /**
     *
     */
    private LocalDateTime  createTime;

    /**
     *
     */
    private LocalDateTime updateTime;

     /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;
}