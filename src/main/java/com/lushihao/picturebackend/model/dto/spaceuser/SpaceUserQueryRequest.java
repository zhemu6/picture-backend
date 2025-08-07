package com.lushihao.picturebackend.model.dto.spaceuser;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.lushihao.picturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 空间用户查询请求
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-02   20:37
 */

@Data
public class SpaceUserQueryRequest implements Serializable {

    private static final long serialVersionUID = 3192509651458666255L;
    /**
     * id
     */
    private Long id;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;


}
