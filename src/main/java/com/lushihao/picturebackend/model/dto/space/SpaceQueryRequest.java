package com.lushihao.picturebackend.model.dto.space;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.lushihao.picturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 空间查询请求 允许通过id 通过空间名称 通过空间的级别 以及用户的id来查询 同时集成分页查询请求类 我们需要支持分页查询业务功能开发
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-02   20:37
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class SpaceQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = -2992755648414718171L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;


    /**
     * 空间类型：0-私有 1-团队
     */
    private Integer spaceType;


}
