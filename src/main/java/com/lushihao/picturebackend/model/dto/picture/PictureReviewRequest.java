package com.lushihao.picturebackend.model.dto.picture;

import com.lushihao.picturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 图片审核请求
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-31   10:51
 */
@Data
public class PictureReviewRequest implements Serializable {


    private static final long serialVersionUID = 5276485175274472982L;

    /**
     * id
     */
    private Long id;


    /**
     * 状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;


}
