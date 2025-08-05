package com.lushihao.picturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.picturebackend.exception.ErrorCode;
import com.lushihao.picturebackend.exception.ThrowUtils;
import com.lushihao.picturebackend.model.dto.space.SpaceAddRequest;
import com.lushihao.picturebackend.model.dto.space.SpaceQueryRequest;
import com.lushihao.picturebackend.model.entity.Space;
import com.lushihao.picturebackend.model.entity.User;
import com.lushihao.picturebackend.model.enums.SpaceLevelEnum;
import com.lushihao.picturebackend.model.vo.SpaceVO;
import com.lushihao.picturebackend.model.vo.UserVO;
import com.lushihao.picturebackend.service.SpaceService;
import com.lushihao.picturebackend.mapper.SpaceMapper;
import com.lushihao.picturebackend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author lushihao
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-08-02 20:29:16
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {


    @Resource
    private UserService userService;
    // 编程性事农
    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 根据查询条件古剑LambdaQueryWrapper对象
     *
     * @param spaceQueryRequest 查询条件
     * @return LambdaQueryWrapper对象
     */
    @Override
    public LambdaQueryWrapper<Space> getLambdaQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAMS_ERROR);

        Long id = spaceQueryRequest.getId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Long userId = spaceQueryRequest.getUserId();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();

        LambdaQueryWrapper<Space> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(id), Space::getId, id);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(userId), Space::getUserId, userId);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), Space::getSpaceLevel, spaceLevel);
        lambdaQueryWrapper.like(StrUtil.isNotBlank(spaceName), Space::getSpaceName, spaceName);


        final Map<String, SFunction<Space, ?>> sortFieldMap;
        Map<String, SFunction<Space, ?>> map = new HashMap<>();
        map.put("id", Space::getId);
        map.put("userId", Space::getUserId);
        map.put("spaceName", Space::getSpaceName);
        map.put("spaceLevel", Space::getSpaceLevel);


        sortFieldMap = Collections.unmodifiableMap(map);

        if (StrUtil.isNotEmpty(sortField)) {
            boolean isAsc = "ascend".equalsIgnoreCase(sortOrder);
            SFunction<Space, ?> sortFunc = sortFieldMap.get(sortField);
            if (sortFunc != null) {
                lambdaQueryWrapper.orderBy(true, isAsc, sortFunc);
            }
        }

        return lambdaQueryWrapper;
    }

    /**
     * 空间校验
     *
     * @param space
     */
    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);

        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();

        if(add){
            // 创建时校验
            ThrowUtils.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            ThrowUtils.throwIf(spaceLevel == null, ErrorCode.PARAMS_ERROR, "空间等级不能为空");
        }
        // 修改数据时 对空间名称和空间级别进行校验
        ThrowUtils.throwIf(StrUtil.isNotBlank(spaceName)&&spaceName.length()>30, ErrorCode.PARAMS_ERROR, "空间名称太长，请重新输入！");
        ThrowUtils.throwIf(spaceLevel!=null&&SpaceLevelEnum.getEnumByValue(spaceLevel) == null, ErrorCode.PARAMS_ERROR, "空间等级不存在");
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
        if(userId != null && userId > 0){
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
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
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        // 2.填充信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum enumByValue = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if(enumByValue!=null){
            // 如果管理员传入的max size 和maxCount 不为空 则使用管理员设置的值 否则采用原来的
            if(space.getMaxSize()==null){
                space.setMaxSize(enumByValue.getMaxSize());
            }
            if(space.getMaxCount()==null){
                space.setMaxCount(enumByValue.getMaxCount());
            }


        }

        ThrowUtils.throwIf(enumByValue== null, ErrorCode.PARAMS_ERROR, "空间等级不存在");
    }


    Map<Long,Object> lockMap = new ConcurrentHashMap<>();

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        Space space = new Space();
        BeanUtil.copyProperties(spaceAddRequest, space);
        // 如果空间名称为空
        if(StrUtil.isBlank(space.getSpaceName())){
            space.setSpaceName("默认空间！");
        }
        // 空间级别为空
        if(space.getSpaceLevel()==null){
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        this.fillSpaceBySpaceLevel(space);
        // 校验参数默认值 是否是添加 设置为true
        this.validSpace(space, true);
        // 权限校验 非管理员 智能够创建普通版的空间
        Long loginUserId = loginUser.getId();
        space.setUserId(loginUserId);
        // 如果用户创建的级别不等于common 并且用户的权限不是admin
        ThrowUtils.throwIf(!space.getSpaceLevel().equals(SpaceLevelEnum.COMMON.getValue())&& !userService.isAdmin(loginUser),ErrorCode.NO_AUTH_ERROR,"无权限创建指定级别空间");

        // 一个用户只能创建一个私有空间
        // 创建一个锁 每个用户一把锁
        Object lock = lockMap.computeIfAbsent(loginUserId,key ->new Object());

        // 加锁
        synchronized (lock){
            try{
                // 数据库相关操作
                Long newSpaceId = transactionTemplate.execute(status -> {
                    // 判断是否已经有私有空间
                    boolean exists = this.lambdaQuery().eq(Space::getUserId, loginUserId).exists();
                    ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "一个用户只能创建一个私有空间");
                    // 创建
                    boolean save = this.save(space);
                    ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "空间创建失败，请稍后重试");
                    return space.getId();
                });
                return  newSpaceId;
            }finally {
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
        ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "无权限操作");

    }



}




