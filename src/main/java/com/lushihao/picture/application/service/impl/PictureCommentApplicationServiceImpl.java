package com.lushihao.picture.application.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.picture.application.service.PictureApplicationService;
import com.lushihao.picture.application.service.PictureCommentApplicationService;
import com.lushihao.picture.application.service.UserApplicationService;
import com.lushihao.picture.domain.picture.entity.Picture;
import com.lushihao.picture.domain.picture.entity.PictureComment;
import com.lushihao.picture.domain.picture.service.PictureCommentDomainService;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.infrastructure.common.DeleteRequest;
import com.lushihao.picture.infrastructure.exception.ErrorCode;
import com.lushihao.picture.infrastructure.exception.ThrowUtils;
import com.lushihao.picture.interfaces.dto.picture.PictureCommentAddRequest;
import com.lushihao.picture.interfaces.dto.picture.PictureCommentQueryRequest;
import com.lushihao.picture.interfaces.vo.picture.PictureCommentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author lushihao
 * @description 针对表【picture_comment(图片评论表（支持楼中楼）)】的数据库操作Service实现
 * @createDate 2025-08-01 20:27:36
 */
@Slf4j
@Service
public class PictureCommentApplicationServiceImpl implements PictureCommentApplicationService {

    @Resource
    private PictureCommentDomainService pictureCommentDomainService;

    @Resource
    private UserApplicationService userApplicationService;

    @Resource
    private PictureApplicationService pictureApplicationService;

    /**
     * 创建评论
     *
     * @param pictureCommentAddRequest 发表评论请求
     * @param loginUser                  用于获取登陆用户
     * @return 创建的评论id
     */
    @Override
    public long addPictureComment(PictureCommentAddRequest pictureCommentAddRequest, User loginUser) {
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
        Picture picture = pictureApplicationService.getById(pictureId);
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

    private boolean save(PictureComment pictureComment) {
        return pictureCommentDomainService.save(pictureComment);
    }

    private PictureComment getById(Long parentId) {
        return pictureCommentDomainService.getById(parentId);
    }

    /**
     * 删除评论
     *
     * @param deleteRequest 删除请求
     * @param loginUser       获取登录用户
     * @return 是否删除成功
     */
    @Override
    public boolean deletePictureComment(DeleteRequest deleteRequest, User loginUser) {
        return  pictureCommentDomainService.deletePictureComment(deleteRequest, loginUser);
    }

    /**
     * 获取查询条件 根据用户传入的参数 构造SQL查询
     *
     * @param pictureCommentQueryRequest 前端传入查询的参数
     * @return 查询到的对象
     */
    @Override
    public LambdaQueryWrapper<PictureComment> getLambdaQueryWrapper(PictureCommentQueryRequest pictureCommentQueryRequest) {
        return pictureCommentDomainService.getLambdaQueryWrapper(pictureCommentQueryRequest);
    }


    /**
     * 分页获取评论VO类 这里是只查询一级评论 也就是一个图片的所有
     *
     * @param pictureCommentPage 分页对象
     * @param request            请求
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
        User loginUser = userApplicationService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_FOUND_ERROR, "登录查看评论");
        Long loginUserId = loginUser.getId();

        // 批量获取所有评论用户ID
        Set<Long> userIdSet = pictureCommentList.stream()
                .map(PictureComment::getUserId)
                .collect(Collectors.toSet());

        // 查询所有用户信息
        Map<Long, User> userMap = userApplicationService.listByIds(userIdSet).stream()
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

    @Override
    public Page<PictureComment> page(Page<PictureComment> pictureCommentPage, LambdaQueryWrapper<PictureComment> lambdaQueryWrapper) {
        return pictureCommentDomainService.page(pictureCommentPage,lambdaQueryWrapper);
    }


}




