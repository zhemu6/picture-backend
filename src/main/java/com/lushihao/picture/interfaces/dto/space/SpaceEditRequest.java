package com.lushihao.picture.interfaces.dto.space;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * 空间编辑请求 对用户使用 仅支持修改空间名称
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-02   20:34
 */
@Data
public class SpaceEditRequest implements Serializable {

    private static final long serialVersionUID = -333219983941919666L;

    /**
     * id 要编辑哪个空间
     */
    private Long id;
    /**
     * 空间名称 编辑的名称
     */
    private String spaceName;
}
