package com.lushihao.picturebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.picturebackend.model.entity.PictureComment;
import com.lushihao.picturebackend.service.PictureCommentService;
import com.lushihao.picturebackend.mapper.PictureCommentMapper;
import org.springframework.stereotype.Service;

/**
* @author lushihao
* @description 针对表【picture_comment(图片评论表（支持楼中楼）)】的数据库操作Service实现
* @createDate 2025-08-01 20:27:36
*/
@Service
public class PictureCommentServiceImpl extends ServiceImpl<PictureCommentMapper, PictureComment>
    implements PictureCommentService{

}




