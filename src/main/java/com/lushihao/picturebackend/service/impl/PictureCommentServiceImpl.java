package com.lushihao.picturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.picturebackend.common.DeleteRequest;
import com.lushihao.picturebackend.exception.ErrorCode;
import com.lushihao.picturebackend.exception.ThrowUtils;
import com.lushihao.picturebackend.model.dto.pictureComment.PictureCommentAddRequest;
import com.lushihao.picturebackend.model.dto.pictureComment.PictureCommentQueryRequest;
import com.lushihao.picturebackend.model.entity.Picture;
import com.lushihao.picturebackend.model.entity.PictureComment;
import com.lushihao.picturebackend.model.entity.Space;
import com.lushihao.picturebackend.model.entity.User;
import com.lushihao.picturebackend.model.vo.PictureCommentVO;
import com.lushihao.picturebackend.model.vo.SpaceVO;
import com.lushihao.picturebackend.service.PictureCommentService;
import com.lushihao.picturebackend.mapper.PictureCommentMapper;
import com.lushihao.picturebackend.service.PictureService;
import com.lushihao.picturebackend.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author lushihao
 * @description 针对表【picture_comment(图片评论表（支持楼中楼）)】的数据库操作Service实现
 * @createDate 2025-08-01 20:27:36
 */
@Service
public class PictureCommentServiceImpl extends ServiceImpl<PictureCommentMapper, PictureComment>
        implements PictureCommentService {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    /**
     * 创建评论
     *
     * @param pictureCommentAddRequest 发表评论请求
     * @param request                  用于获取登陆用户
     * @return 创建的评论id
     */
    @Override
    public long addPictureComment(PictureCommentAddRequest pictureCommentAddRequest, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        // 1. 权限校验
        // 1.1 用户需要登录
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "您未登录，请登录后发表评论！");
        ThrowUtils.throwIf(pictureCommentAddRequest == null, ErrorCode.PARAMS_ERROR, "评论内容不能为空！");
        Long pictureId = pictureCommentAddRequest.getPictureId();
        String content = pictureCommentAddRequest.getContent();
        Long parentId = pictureCommentAddRequest.getParentId();
        // 1.2 基础参数校验
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR, "图片ID错误");
        ThrowUtils.throwIf(StrUtil.isBlank(content), ErrorCode.PARAMS_ERROR, "评论内容不能为空");
        // 1.3 校验图片是否还存在
        Picture picture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR, "图片不存在");
        // 1.4 如果是子评论 我们需要校验父评论是否被删除
        if (parentId != null && parentId > 0) {
            PictureComment parentComment = this.getById(parentId);
            ThrowUtils.throwIf(parentComment == null, ErrorCode.PARAMS_ERROR, "父评论不存在");
        }
        // 2. 插入评论
        PictureComment pictureComment = new PictureComment();
        pictureComment.setParentId(parentId != null ? parentId : 0L);
        pictureComment.setPictureId(pictureId);
        pictureComment.setContent(content);
        pictureComment.setUserId(loginUser.getId());
        boolean isSave = this.save(pictureComment);
        ThrowUtils.throwIf(!isSave, ErrorCode.OPERATION_ERROR, "评论失败");
        return pictureComment.getId();
    }

    /**
     * 删除评论
     *
     * @param deleteRequest 删除请求
     * @param request       获取登录用户
     * @return 是否删除成功
     */
    @Override
    public boolean deletePictureComment(DeleteRequest deleteRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        // 1.参数校验
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        long commentId = deleteRequest.getId();
        PictureComment comment = this.getById(commentId);
        ThrowUtils.throwIf(comment == null, ErrorCode.NOT_FOUND_ERROR);
        // 2. 权限校验仅有自己或者是管理员可以删除评论
        ThrowUtils.throwIf(!userService.isAdmin(loginUser) || !comment.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限删除该评论");
        // 操作数据库 逻辑删除
        boolean isDelete = this.removeById(commentId);
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

        sortFieldMap = Collections.unmodifiableMap(map);

        if (StrUtil.isNotEmpty(sortField)) {
            boolean isAsc = "ascend".equalsIgnoreCase(sortOrder);
            SFunction<PictureComment, ?> sortFunc = sortFieldMap.get(sortField);
            if (sortFunc != null) {
                lambdaQueryWrapper.orderBy(true, isAsc, sortFunc);
            }
        }

        return lambdaQueryWrapper;

    }


    /**
     * 分页获取评论VO类 这里是只查询一级评论 也就是一个图片的所有
     *
     * @param pictureCommentPage 分页对象
     * @param request   请求
     * @return 分页VO对象
     */
    @Override
    public Page<PictureCommentVO> getPictureCommentVOPage(Page<PictureComment> pictureCommentPage, HttpServletRequest request) {
        List<PictureComment> pictureCommentList = pictureCommentPage.getRecords();

        Page<PictureCommentVO> pictureCommentVOPage = new Page<>(
                pictureCommentPage.getCurrent(),
                pictureCommentPage.getSize(),
                pictureCommentPage.getTotal()
        );

        if (CollUtil.isEmpty(pictureCommentList)) {
            return pictureCommentVOPage;
        }

        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_FOUND_ERROR, "登录查看评论");
        Long loginUserId = loginUser.getId();

        // 批量获取所有评论用户ID
        Set<Long> userIdSet = pictureCommentList.stream()
                .map(PictureComment::getUserId)
                .collect(Collectors.toSet());

        // 查询所有用户信息
        Map<Long, User> userMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 构造 VO 列表
        List<PictureCommentVO> voList = pictureCommentList.stream().map(comment -> {
            PictureCommentVO vo = PictureCommentVO.objToVo(comment);

            // 设置用户信息
            User user = userMap.get(comment.getUserId());
            if (user != null) {
                vo.setUserName(user.getUserName());
                vo.setUserAvatar(user.getUserAvatar());
            }

            // 设置是否为本人
            vo.setIsSelf(loginUserId.equals(comment.getUserId()));

            return vo;
        }).collect(Collectors.toList());

        pictureCommentVOPage.setRecords(voList);
        return pictureCommentVOPage;
    }


}




