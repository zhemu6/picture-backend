package com.lushihao.picturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.picturebackend.exception.ErrorCode;
import com.lushihao.picturebackend.exception.ThrowUtils;
import com.lushihao.picturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.lushihao.picturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.lushihao.picturebackend.model.entity.Space;
import com.lushihao.picturebackend.model.entity.SpaceUser;
import com.lushihao.picturebackend.model.entity.User;
import com.lushihao.picturebackend.model.enums.SpaceRoleEnum;
import com.lushihao.picturebackend.model.vo.SpaceUserVO;
import com.lushihao.picturebackend.model.vo.SpaceVO;
import com.lushihao.picturebackend.model.vo.UserVO;
import com.lushihao.picturebackend.service.SpaceService;
import com.lushihao.picturebackend.service.SpaceUserService;
import com.lushihao.picturebackend.mapper.SpaceUserMapper;
import com.lushihao.picturebackend.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lushihao
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
 * @createDate 2025-08-06 14:29:25
 */
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
        implements SpaceUserService {
    @Resource
    private UserService userService;
    @Resource
    @Lazy
    private SpaceService spaceService;


    /**
     * 根据查询条件古剑LambdaQueryWrapper对象
     *
     * @param spaceUserQueryRequest 查询条件
     * @return LambdaQueryWrapper对象
     */
    @Override
    public LambdaQueryWrapper<SpaceUser> getLambdaQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);

        Long id = spaceUserQueryRequest.getId();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        String spaceRole = spaceUserQueryRequest.getSpaceRole();

        LambdaQueryWrapper<SpaceUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(id), SpaceUser::getId, id);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(spaceId), SpaceUser::getSpaceId, spaceId);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(userId), SpaceUser::getUserId, userId);
        lambdaQueryWrapper.eq(StrUtil.isNotEmpty(spaceRole), SpaceUser::getSpaceRole, spaceRole);

        return lambdaQueryWrapper;
    }

    /**
     * 空间校验
     *
     * @param spaceUser
     */
    @Override
    public void validSpaceUser(SpaceUser spaceUser, boolean add) {
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.PARAMS_ERROR);
        // 校验空间用户表
        Long spaceId = spaceUser.getSpaceId();
        Long userId = spaceUser.getUserId();
        if (add) {
            // 如果是添加 我们需要确保spaceId和userId不能为空
            ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR, "spaceId和userId不能为空");
            User user = userService.getById(userId);
            ThrowUtils.throwIf(user == null || userId <= 0, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null || spaceId <= 0, ErrorCode.NOT_FOUND_ERROR, "空间不存在不存在");
            // 从当前的space_user表中查询是否已经存在
            SpaceUser oned = this.lambdaQuery().eq(SpaceUser::getSpaceId, spaceId).eq(SpaceUser::getUserId, userId).one();
            ThrowUtils.throwIf(oned != null, ErrorCode.PARAMS_ERROR, "该用户已经存在");
        }
        // 校验空间权限
        String spaceRole = spaceUser.getSpaceRole();
        SpaceRoleEnum spaceRoleEnum = SpaceRoleEnum.getEnumByValue(spaceRole);
        // 如果spaceRole为空 或者是枚举值为空
        ThrowUtils.throwIf(spaceRole != null && spaceRoleEnum == null, ErrorCode.PARAMS_ERROR, "空间角色不存在");

    }

    /**
     * 获取空间VO类
     *
     * @param spaceUser 传入一个space
     * @param request   请求
     * @return SpaceVO类
     */
    @Override
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request) {
        // 首先将space转换成VO类
        SpaceUserVO spaceUserVO = SpaceUserVO.objToVo(spaceUser);
        // 关联查询用户信息
        Long userId = spaceUser.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceUserVO.setUser(userVO);
        }
        // 关联查询空间信息
        Long spaceId = spaceUser.getSpaceId();
        if (spaceId != null && spaceId > 0) {
            Space space = spaceService.getById(spaceId);
            SpaceVO spaceVO = spaceService.getSpaceVO(space, request);
            spaceUserVO.setSpace(spaceVO);
        }

        return spaceUserVO;
    }

    /**
     * List获取图片VO类
     *
     * @param spaceUserList
     * @return List VO对象
     */
    @Override
    public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList) {
        // 如果为空 返回空列表
        if (CollUtil.isEmpty(spaceUserList)) {
            return Collections.emptyList();
        }
        // 对象列表 => 封装对象列表
        List<SpaceUserVO> spaceUserVOList = spaceUserList.stream().map(SpaceUserVO::objToVo).collect(Collectors.toList());
        // 1. 收集需要关联查询的用户 ID 和空间 ID
        Set<Long> userIdSet = spaceUserList.stream().map(SpaceUser::getUserId).collect(Collectors.toSet());
        Set<Long> spaceIdSet = spaceUserList.stream().map(SpaceUser::getSpaceId).collect(Collectors.toSet());
        //2. 批量查询用户和空间
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        Map<Long, List<Space>> spaceIdSpaceListMap = spaceService.listByIds(spaceIdSet).stream().collect(Collectors.groupingBy(Space::getId));
        // 3.填充信息
        spaceUserVOList.forEach(spaceUserVO -> {
            Long userId = spaceUserVO.getUserId();
            Long spaceId = spaceUserVO.getSpaceId();
            User user = null;
            Space space = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            if (spaceIdSpaceListMap.containsKey(spaceId)) {
                space = spaceIdSpaceListMap.get(spaceId).get(0);
            }
            spaceUserVO.setUser(userService.getUserVO(user));
            spaceUserVO.setSpace(SpaceVO.objToVo(space));
        });
        return spaceUserVOList;
    }

    /**
     * 创建空间成员
     *
     * @param spaceUserAddRequest 空间
     * @return 创建的space user表中数据id
     */
    @Override
    public long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
        // 获取登录用户
        SpaceUser spaceUser = new SpaceUser();
        BeanUtil.copyProperties(spaceUserAddRequest, spaceUser);
        validSpaceUser(spaceUser, true);

        boolean save = this.save(spaceUser);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "空间成员创建失败，请稍后重试");
        return spaceUser.getId();

    }


}




