package com.lushihao.picture.interfaces.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-01   8:40
 */
@Data
public class PictureUploadByBatchRequest implements Serializable {

    private static final long serialVersionUID = 5288358433097151182L;

    /*
     * 搜索词
     */
    private String searchText;

    /*
     * 抓取数量
     */
    private Integer count;
    /*
     * 前缀
     */
    private String namePrefix;

    /*
     * 分类
     */
    private String category;

    /*
     * 标签
     */
    private List<String> tags;


}
