package com.lushihao.picture.interfaces.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.picture.application.service.SpaceApplicationService;
import com.lushihao.picture.application.service.UserApplicationService;
import com.lushihao.picture.domain.space.entity.Space;
import com.lushihao.picture.domain.space.valueobject.SpaceLevelEnum;
import com.lushihao.picture.domain.user.constant.UserConstant;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.infrastructure.annotation.AuthCheck;
import com.lushihao.picture.infrastructure.common.BaseResponse;
import com.lushihao.picture.infrastructure.common.DeleteRequest;
import com.lushihao.picture.infrastructure.common.ResultUtils;
import com.lushihao.picture.infrastructure.exception.ErrorCode;
import com.lushihao.picture.infrastructure.exception.ThrowUtils;
import com.lushihao.picture.interfaces.assemble.SpaceAssembler;
import com.lushihao.picture.interfaces.dto.space.*;
import com.lushihao.picture.interfaces.vo.space.SpaceVO;
import com.lushihao.picture.shared.auth.SpaceUserAuthManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户空间控制层
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-02   20:54
 */
@Slf4j
@RequestMapping("/space")
@RestController
public class SpaceController {

    @Resource
    private SpaceApplicationService spaceApplicationService;

    @Resource
    private UserApplicationService userApplicationService;
    @Autowired
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 用户创建空间
     *
     * @param spaceAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userApplicationService.getLoginUser(request);
        // 获取登录用户
        Integer spaceType = spaceAddRequest.getSpaceType();
        Space space = SpaceAssembler.toSpaceEntity(spaceAddRequest);
        long spaceId = spaceApplicationService.addSpace(space, spaceType, loginUser);
        return ResultUtils.success(spaceId);
    }


    /**
     * 删除空间（管理员可用）
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userApplicationService.getLoginUser(request);
        Long spaceId = deleteRequest.getId();
        spaceApplicationService.deleteSpace(spaceId, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 更新空间（管理员可用）
     *
     * @param spaceUpdateRequest 空间更新请求
     * @param request            请求（用于获取登录用户）
     * @return 是否更新成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 将实体类和DTO转换
        Space space = SpaceAssembler.toSpaceEntity(spaceUpdateRequest);
        // 自动填充数据
        spaceApplicationService.fillSpaceBySpaceLevel(space);
        // 数据校验 不是创建服务
        space.validSpace(false);
        // 判断是否存在
        long id = spaceUpdateRequest.getId();
        // 判断是否存在
        Space oldSpace = spaceApplicationService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean isUpdate = spaceApplicationService.updateById(space);
        ThrowUtils.throwIf(!isUpdate, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpaceById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Space space = spaceApplicationService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(space);
    }

    /**
     * 根据 id 获取空间（封装类）这里 应该是仅用户本身能够访问
     */
    @GetMapping("/get/vo")
    public BaseResponse<SpaceVO> getSpaceVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Space space = spaceApplicationService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        SpaceVO spaceVO = spaceApplicationService.getSpaceVO(space, request);
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, userApplicationService.getLoginUser(request));
        spaceVO.setPermissionList(permissionList);
        return ResultUtils.success(spaceVO);
    }

    /**
     * 分页获取空间列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        //查询数据库
        Page<Space> spacePage = spaceApplicationService.page(new Page<>(current, size), spaceApplicationService.getLambdaQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(spacePage);
    }

    /**
     * 分页获取空间列表（封装类）
     * 提供给普通用户
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Space> spacePage = spaceApplicationService.page(new Page<>(current, size), spaceApplicationService.getLambdaQueryWrapper(spaceQueryRequest));

        return ResultUtils.success(spaceApplicationService.getSpaceVOPage(spacePage, request));
    }


    /**
     * 编辑空间
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest editRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(editRequest == null || editRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 将实体类和DTO转换
        Space space = SpaceAssembler.toSpaceEntity(editRequest);
        // 自动填充数据
        spaceApplicationService.fillSpaceBySpaceLevel(space);
        // 设置编辑时间
        space.setUpdateTime(new Date());
        // 数据校验
        space.validSpace(false);
        User loginUser = userApplicationService.getLoginUser(request);
        // 判断是否存在
        long id = editRequest.getId();
        Space oldSpace = spaceApplicationService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人和管理员可以删除
        spaceApplicationService.checkSpaceAuth(oldSpace, loginUser);
        // 操作数据库更新 根据space有的值去更新数据库中相应位置的标签 此时数据库中是已经上传成功这些空间的
        boolean isEdit = spaceApplicationService.updateById(space);
        ThrowUtils.throwIf(!isEdit, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取所有的空间级别 便于前端进行展示
     *
     * @return
     */
    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> {
                    return new SpaceLevel(spaceLevelEnum.getValue(), spaceLevelEnum.getText(), spaceLevelEnum.getMaxCount(), spaceLevelEnum.getMaxSize());
                }).collect(Collectors.toList());
        return ResultUtils.success(spaceLevelList);
    }


}
