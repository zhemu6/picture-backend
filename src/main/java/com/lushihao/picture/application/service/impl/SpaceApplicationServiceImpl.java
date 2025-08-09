package com.lushihao.picture.application.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.picture.application.service.SpaceApplicationService;
import com.lushihao.picture.application.service.SpaceUserApplicationService;
import com.lushihao.picture.application.service.UserApplicationService;
import com.lushihao.picture.domain.space.entity.Space;
import com.lushihao.picture.domain.space.entity.SpaceUser;
import com.lushihao.picture.domain.space.service.SpaceDomainService;
import com.lushihao.picture.domain.space.valueobject.SpaceLevelEnum;
import com.lushihao.picture.domain.space.valueobject.SpaceRoleEnum;
import com.lushihao.picture.domain.space.valueobject.SpaceTypeEnum;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.infrastructure.exception.ErrorCode;
import com.lushihao.picture.infrastructure.exception.ThrowUtils;
import com.lushihao.picture.interfaces.dto.space.SpaceQueryRequest;
import com.lushihao.picture.interfaces.vo.space.SpaceVO;
import com.lushihao.picture.interfaces.vo.user.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author lushihao
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-08-02 20:29:16
 */
@Service
public class SpaceApplicationServiceImpl implements SpaceApplicationService {

    @Resource
    private SpaceDomainService spaceDomainService;

    @Resource
    private UserApplicationService userApplicationService;
    // 编程性事农
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private SpaceUserApplicationService spaceUserApplicationService;

    /**
     * 根据查询条件古剑LambdaQueryWrapper对象
     *
     * @param spaceQueryRequest 查询条件
     * @return LambdaQueryWrapper对象
     */
    @Override
    public LambdaQueryWrapper<Space> getLambdaQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        return spaceDomainService.getLambdaQueryWrapper(spaceQueryRequest);
    }

    /**
     * 获取空间VO类
     *
     * @param space   传入一个space
     * @param request 请求
     * @return SpaceVO类
     */
    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 首先将space转换成VO类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 获取图片id和登录用户id
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userApplicationService.getUserById(userId);
            UserVO userVO = userApplicationService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    /**
     * 分页获取图片VO类
     *
     * @param spacePage 分页对象
     * @param request   请求
     * @return 分页VO对象
     */
    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        // 对象列表 => 封装对象列表
        List<SpaceVO> spaceVOList = spaceList.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
        // 1.关联查询用户信息
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userApplicationService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        // 2.填充信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userApplicationService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        spaceDomainService.fillSpaceBySpaceLevel(space);
    }


    Map<Long, Object> lockMap = new ConcurrentHashMap<>();

    @Override
    public long addSpace(Space space, Integer spaceType, User loginUser) {
        space.fillDefaultSpace();

        this.fillSpaceBySpaceLevel(space);
        // 校验参数默认值 是否是添加 设置为true
        space.validSpace(true);
        // 权限校验 非管理员 智能够创建普通版的空间
        Long loginUserId = loginUser.getId();
        space.setUserId(loginUserId);
        // 如果用户创建的级别不等于common 并且用户的权限不是admin
        ThrowUtils.throwIf(!space.getSpaceLevel().equals(SpaceLevelEnum.COMMON.getValue()) && !loginUser.isAdmin(), ErrorCode.NO_AUTH_ERROR, "无权限创建指定级别空间");

        // 一个用户只能创建一个私有空间，以及一个团队空间
        // 创建一个锁 每个用户一把锁
        Object lock = lockMap.computeIfAbsent(loginUserId, key -> new Object());

        // 加锁
        synchronized (lock) {
            try {
                // 数据库相关操作
                Long newSpaceId = transactionTemplate.execute(status -> {

                    boolean exists  = spaceDomainService.getSpaceByUserIdAndSpaceType(loginUserId, spaceType);

                    ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "一个用户只能创建一个私有空间、一个团队空间");
                    // 创建
                    boolean save = this.save(space);
                    ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "空间创建失败，请稍后重试");
                    // 创建成功后 如果是团队空间 要向SpaceUser表中插入一条数据 将档期那登录用户作为管理员
                    if (SpaceTypeEnum.TEAM.getValue() == space.getSpaceType()) {
                        SpaceUser spaceUser = new SpaceUser();
                        spaceUser.setSpaceId(space.getId());
                        spaceUser.setUserId(loginUserId);
                        spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                        boolean isSave = spaceUserApplicationService.save(spaceUser);
                        ThrowUtils.throwIf(!isSave, ErrorCode.OPERATION_ERROR, "创建团队成员记录失败，请稍后重试");
                    }
                    // 仅对团队空间生效 创建分表
//                    dynamicShardingManager.createSpacePictureTable(space);
                    return space.getId();
                });
                return newSpaceId;
            } finally {
                // 防止内存泄露
                lockMap.remove(loginUserId);
            }
        }
    }

    /**
     * 校验空间权限 只有当 当前用户是空间的所有者才可以
     *
     * @param space     空间
     * @param loginUser 登录用户
     */
    @Override
    public void checkSpaceAuth(Space space, User loginUser) {
        ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()) && !loginUser.isAdmin(), ErrorCode.NO_AUTH_ERROR, "无权限操作");
    }

    @Override
    public boolean save(Space space){
       return   spaceDomainService.save(space);
    }

    @Override
    public void deleteSpace(Long spaceId,User loginUser) {
        // 判断是否存在
        Space oldSpace = this.getById(spaceId);
        ThrowUtils.throwIf(oldSpace==null,ErrorCode.NOT_FOUND_ERROR);
        // 仅本人和管理员可以删除
        this.checkSpaceAuth(oldSpace, loginUser);
        // 操作数据库删除
        boolean isDelete = this.removeById(spaceId);
        ThrowUtils.throwIf(!isDelete,ErrorCode.OPERATION_ERROR);
    }

    @Override
    public boolean removeById(Long spaceId) {
        return spaceDomainService.removeById(spaceId);
    }

    @Override
    public Space getById(Long spaceId) {
        return spaceDomainService.getById(spaceId);
    }

    @Override
    public boolean updateById(Space space) {
        return spaceDomainService.updateById(space);
    }

    @Override
    public Page<Space> page(Page<Space> spacePage, LambdaQueryWrapper<Space> lambdaQueryWrapper) {
        return spaceDomainService.page(spacePage, lambdaQueryWrapper);
    }

    @Override
    public List<Space>listByIds(Set<Long> spaceIdSet) {
        return spaceDomainService.listByIds(spaceIdSet);
    }

    @Override
    public boolean addUpdatePicture(Long finalSpaceId, Long picSize) {
        return spaceDomainService.addUpdatePicture(finalSpaceId, picSize);
    }

    @Override
    public boolean deleteUpdatePicture(Long spaceId, Long picSize) {
        return spaceDomainService.deleteUpdatePicture(spaceId, picSize);
    }

    @Override
    public List<Space> list(QueryWrapper<Space> queryWrapper) {
        return spaceDomainService.list(queryWrapper);
    }

    @Override
    public Set<Long> getSpaceId(int value) {
        return spaceDomainService.getSpaceId(value);
    }


}




