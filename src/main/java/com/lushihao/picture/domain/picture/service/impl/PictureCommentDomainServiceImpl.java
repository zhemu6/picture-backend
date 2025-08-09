package com.lushihao.picture.domain.picture.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.picture.domain.picture.entity.PictureComment;
import com.lushihao.picture.domain.picture.repository.PictureCommentRepository;
import com.lushihao.picture.domain.picture.service.PictureCommentDomainService;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.infrastructure.common.DeleteRequest;
import com.lushihao.picture.infrastructure.exception.ErrorCode;
import com.lushihao.picture.infrastructure.exception.ThrowUtils;
import com.lushihao.picture.interfaces.dto.picture.PictureCommentQueryRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lushihao
 * @description 针对表【picture_comment(图片评论表（支持楼中楼）)】的数据库操作Service实现
 * @createDate 2025-08-01 20:27:36
 */
@Slf4j
@Service
public class PictureCommentDomainServiceImpl implements PictureCommentDomainService {

    @Resource
    private PictureCommentRepository pictureCommentRepository;


    /**
     * 删除评论
     *
     * @param deleteRequest 删除请求
     * @param loginUser       获取登录用户
     * @return 是否删除成功
     */
    @Override
    public boolean deletePictureComment(DeleteRequest deleteRequest, User loginUser) {
        // 1.参数校验
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        long commentId = deleteRequest.getId();
        PictureComment comment = this.getById(commentId);
        ThrowUtils.throwIf(comment == null, ErrorCode.NOT_FOUND_ERROR);
        // 2. 权限校验仅有自己或者是管理员可以删除评论
        ThrowUtils.throwIf(!loginUser.isAdmin() || !comment.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限删除该评论");
        // 操作数据库 逻辑删除
        boolean isDelete = pictureCommentRepository.removeById(commentId);
        ThrowUtils.throwIf(!isDelete, ErrorCode.OPERATION_ERROR, "删除评论失败");
        return true;
    }

    /**
     * 获取查询条件 根据用户传入的参数 构造SQL查询
     *
     * @param pictureCommentQueryRequest 前端传入查询的参数
     * @return 查询到的对象
     */
    @Override
    public LambdaQueryWrapper<PictureComment> getLambdaQueryWrapper(PictureCommentQueryRequest pictureCommentQueryRequest) {

        ThrowUtils.throwIf(pictureCommentQueryRequest == null, ErrorCode.PARAMS_ERROR);

        Long id = pictureCommentQueryRequest.getId();
        Long userId = pictureCommentQueryRequest.getUserId();
        Long pictureId = pictureCommentQueryRequest.getPictureId();
        String content = pictureCommentQueryRequest.getContent();
        Long parentId = pictureCommentQueryRequest.getParentId();

        String sortField = pictureCommentQueryRequest.getSortField();
        String sortOrder = pictureCommentQueryRequest.getSortOrder();

        // 定义一个新的LambdaQueryWrapper
        LambdaQueryWrapper<PictureComment> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 如果是用户的ID或者是权限 我们需要精准的查询
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(id), PictureComment::getId, id);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(userId), PictureComment::getUserId, userId);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(pictureId), PictureComment::getPictureId, pictureId);
        lambdaQueryWrapper.like(StrUtil.isNotBlank(content), PictureComment::getContent, content);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(parentId), PictureComment::getParentId, parentId);


        final Map<String, SFunction<PictureComment, ?>> sortFieldMap;
        Map<String, SFunction<PictureComment, ?>> map = new HashMap<>();

        map.put("id", PictureComment::getId);
        map.put("userId", PictureComment::getUserId);
        map.put("pictureId", PictureComment::getPictureId);
        map.put("content", PictureComment::getContent);
        map.put("parentId", PictureComment::getParentId);
        map.put("createTime", PictureComment::getCreateTime);
        sortFieldMap = Collections.unmodifiableMap(map);
        log.info("排序字段为：{}，排序方向为：{}", sortField, sortOrder);


        // 如果排序字段非空
        if (StrUtil.isNotEmpty(sortField)) {
            boolean isAsc = "ascend".equalsIgnoreCase(sortOrder);
            SFunction<PictureComment, ?> sortFunc = sortFieldMap.get(sortField);
            if (sortFunc != null) {
                lambdaQueryWrapper.orderBy(true, isAsc, sortFunc);
            }
        }

        return lambdaQueryWrapper;

    }


    @Override
    public PictureComment getById(Long parentId) {
        return pictureCommentRepository.getById(parentId);
    }

    @Override
    public boolean save(PictureComment pictureComment) {
        return pictureCommentRepository.save(pictureComment);
    }

    @Override
    public Page<PictureComment> page(Page<PictureComment> pictureCommentPage, LambdaQueryWrapper<PictureComment> lambdaQueryWrapper) {
        return pictureCommentRepository.page(pictureCommentPage, lambdaQueryWrapper);
    }


}




