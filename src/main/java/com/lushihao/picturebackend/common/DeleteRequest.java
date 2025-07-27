package com.lushihao.picturebackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求包装类 接受需要删除数据的id作为参数
 * @author lushihao
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;

}
