package com.lushihao.picture.interfaces.dto.space;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 创建空间请求 只需要啊传的入创建空间的名称和级别即可
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-02   20:32
 */
@Data
public class SpaceAddRequest implements Serializable {

    private static final long serialVersionUID = -8218371246056711084L;
    /**
     * id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;


    /**
     * 空间类型：0-私有 1-团队
     */
    private Integer spaceType;


}
