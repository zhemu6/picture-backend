package com.lushihao.picturebackend.model.dto.spaceuser;

import lombok.Data;

import java.io.Serializable;

/**
 * 编辑空间成员
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-06   14:32
 */
@Data
public class SpaceUserEditRequest implements Serializable {


    private static final long serialVersionUID = -1175270878417947107L;
    /**
     * 修改数据的记录id
     */
    private Long id;


    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

}
