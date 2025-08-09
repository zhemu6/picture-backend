package com.lushihao.picture.application.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lushihao.picture.application.service.SpaceApplicationService;
import com.lushihao.picture.application.service.SpaceUserApplicationService;
import com.lushihao.picture.application.service.UserApplicationService;
import com.lushihao.picture.domain.space.entity.Space;
import com.lushihao.picture.domain.space.entity.SpaceUser;
import com.lushihao.picture.domain.space.service.SpaceUserDomainService;
import com.lushihao.picture.domain.space.valueobject.SpaceRoleEnum;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.infrastructure.exception.ErrorCode;
import com.lushihao.picture.infrastructure.exception.ThrowUtils;
import com.lushihao.picture.interfaces.dto.spaceuser.SpaceUserQueryRequest;
import com.lushihao.picture.interfaces.vo.space.SpaceUserVO;
import com.lushihao.picture.interfaces.vo.space.SpaceVO;
import com.lushihao.picture.interfaces.vo.user.UserVO;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author lushihao
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
 * @createDate 2025-08-06 14:29:25
 */
@Service
public class SpaceUserApplicationServiceImpl implements SpaceUserApplicationService {

    @Resource
    private SpaceUserDomainService spaceUserDomainService;

    @Resource
    private UserApplicationService userApplicationService;
    @Resource
    @Lazy
    private SpaceApplicationService spaceApplicationService;


    /**
     * 根据查询条件古剑LambdaQueryWrapper对象
     *
     * @param spaceUserQueryRequest 查询条件
     * @return LambdaQueryWrapper对象
     */
    @Override
    public LambdaQueryWrapper<SpaceUser> getLambdaQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        return spaceUserDomainService.getLambdaQueryWrapper(spaceUserQueryRequest);
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
            User user = userApplicationService.getUserById(userId);
            ThrowUtils.throwIf(user == null || userId <= 0, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            Space space = spaceApplicationService.getById(spaceId);
            ThrowUtils.throwIf(space == null || spaceId <= 0, ErrorCode.NOT_FOUND_ERROR, "空间不存在不存在");
            // 从当前的space_user表中查询是否已经存在
//            SpaceUser oned = this.lambdaQuery().eq(SpaceUser::getSpaceId, spaceId).eq(SpaceUser::getUserId, userId).one();
            SpaceUser oned = spaceUserDomainService.getByUserIdAndSpaceId(userId, spaceId);


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
            User user = userApplicationService.getUserById(userId);
            UserVO userVO = userApplicationService.getUserVO(user);
            spaceUserVO.setUser(userVO);
        }
        // 关联查询空间信息
        Long spaceId = spaceUser.getSpaceId();
        if (spaceId != null && spaceId > 0) {
            Space space = spaceApplicationService.getById(spaceId);
            SpaceVO spaceVO = spaceApplicationService.getSpaceVO(space, request);
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
        Map<Long, List<User>> userIdUserListMap = userApplicationService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        Map<Long, List<Space>> spaceIdSpaceListMap = spaceApplicationService.listByIds(spaceIdSet).stream().collect(Collectors.groupingBy(Space::getId));
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
            spaceUserVO.setUser(userApplicationService.getUserVO(user));
            spaceUserVO.setSpace(SpaceVO.objToVo(space));
        });
        return spaceUserVOList;
    }

    /**
     * 创建空间成员
     *
     * @return 创建的space user表中数据id
     */
    @Override
    public long addSpaceUser(SpaceUser spaceUser) {
        validSpaceUser(spaceUser, true);
        boolean save = this.save(spaceUser);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "空间成员创建失败，请稍后重试");
        return spaceUser.getId();
    }


    @Override
    public boolean save(SpaceUser spaceUser) {
        return spaceUserDomainService.save(spaceUser);
    }

    @Override
    public void deleteSpaceUser(long spaceId) {
        // 判断是否存在
        SpaceUser oldSpaceUser = this.getById(spaceId);
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库删除
        boolean isDelete = this.removeById(spaceId);
        ThrowUtils.throwIf(!isDelete, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public SpaceUser getOne(LambdaQueryWrapper<SpaceUser> lambdaQueryWrapper) {
        return spaceUserDomainService.getOne(lambdaQueryWrapper);
    }

    @Override
    public List<SpaceUser> list(LambdaQueryWrapper<SpaceUser> lambdaQueryWrapper) {
        return  spaceUserDomainService.list(lambdaQueryWrapper);
    }

    private boolean removeById(long spaceId) {
        return spaceUserDomainService.removeById(spaceId);
    }

    @Override
    public SpaceUser getById(long spaceId) {
        return spaceUserDomainService.getById(spaceId);
    }

    @Override
    public boolean updateById(SpaceUser spaceUser) {
        return  spaceUserDomainService.updateById(spaceUser);
    }

    @Override
    public SpaceUser getSpaceUserBySpaceIdAndUserId(Long spaceId, Long userId) {
        return spaceUserDomainService.getSpaceUserBySpaceIdAndUserId(spaceId,userId);
    }

}




