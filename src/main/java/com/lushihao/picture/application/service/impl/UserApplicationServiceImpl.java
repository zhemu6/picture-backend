package com.lushihao.picture.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.lushihao.picture.domain.user.service.UserDomainService;
import com.lushihao.picture.infrastructure.common.DeleteRequest;
import com.lushihao.picture.interfaces.dto.user.*;
import com.lushihao.picture.infrastructure.exception.ErrorCode;
import com.lushihao.picture.infrastructure.exception.ThrowUtils;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.interfaces.vo.user.LoginUserVO;
import com.lushihao.picture.interfaces.vo.user.UserVO;
import com.lushihao.picture.application.service.UserApplicationService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Set;


/**
 * @author lushihao
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2025-07-27 16:35:43
 */
@Service
public class UserApplicationServiceImpl implements UserApplicationService {

    @Resource
    private UserDomainService userDomainService;

    /**
     * 用户注册功能
     *
     * @param userRegisterRequest 用户注册封装
     * @return 新用户的id
     */
    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        // 校验
        User.validUserRegister(userRegisterRequest);
        // 执行注册
        return userDomainService.userRegister(userRegisterRequest);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录封装类
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        User.validUserLogin(userLoginRequest);
        return userDomainService.userLogin(userLoginRequest, request);
    }

    /**
     * 密码加密
     *
     * @param password 原始密码
     * @return 加密密码
     */
    @Override
    public String getEncryptedPassword(String password) {
        return userDomainService.getEncryptedPassword(password);
    }

    /**
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        return userDomainService.getLoginUser(request);
    }

    /**
     * 根据用户 来获得转换后的LoginUserVO对象
     *
     * @param user 用户
     * @return LoginUserVO
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        return userDomainService.getLoginUserVO(user);
    }

    /**
     * 获取单个用户的脱敏用户对象
     *
     * @param user 用户
     * @return 脱敏用户对象
     */
    @Override
    public UserVO getUserVO(User user) {
        return userDomainService.getUserVO(user);
    }

    /**
     * 获得多个用户对象的脱敏列表
     *
     * @param userList 用户列表
     * @return 脱敏用户列表
     */
    @Override
    public List<UserVO> getUserV0List(List<User> userList) {
        return userDomainService.getUserV0List(userList);
    }

    /**
     * 用户注销
     *
     * @param request 请求
     * @return 是否成功注销
     */
    @Override
    public boolean userLoginOut(HttpServletRequest request) {
        return userDomainService.userLoginOut(request);
    }

    /**
     * 将用户的查询请求类转换成LambdaQueryWrapper对象
     *
     * @param userQueryRequest 用户查询请求类
     * @return LambdaQueryWrapper对象
     */
    @Override
    public LambdaQueryWrapper<User> getLambdaQueryWrapper(UserQueryRequest userQueryRequest) {
        return userDomainService.getLambdaQueryWrapper(userQueryRequest);
    }

    /**
     * 管理员创建单个用户
     *
     * @param userAddRequest 用户请求类
     * @return 新用户的id
     */
    @Override
    public Long addUser(UserAddRequest userAddRequest) {
        return userDomainService.addUser(userAddRequest);
    }

    /**
     * 管理员根据ID查询用户信息（不用脱敏）
     *
     * @param id 用户id
     * @return 查询到的用户信息
     */
    @Override
    public User getUserById(Long id) {
        return userDomainService.getUserById(id);
    }

    /**
     * 根据ID查询用户VO类（脱敏）
     *
     * @param id 用户id
     * @return 查询到的用户信息VO 脱敏
     */
    @Override
    public UserVO getUserVOById(Long id) {
        return userDomainService.getUserVOById(id);
    }

    /**
     * 管理员删除用户
     *
     * @param deleteRequest 删除的包装类
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteUserById(DeleteRequest deleteRequest) {
        return userDomainService.deleteUserById(deleteRequest);
    }

    /**
     * 管理员更新用户
     *
     * @param userUpdateRequest 更新用户包装类
     * @return 是否更新成功
     */
    @Override
    public Boolean updateUser(UserUpdateRequest userUpdateRequest) {
        return userDomainService.updateUser(userUpdateRequest);
    }


    /**
     * 管理员分页获取用户列表 VO类
     *
     * @param userQueryRequest 用户查询类
     * @return UserVO列表
     */
    @Override
    public Page<UserVO> listUserVOByPage(UserQueryRequest userQueryRequest) {
        return userDomainService.listUserVOByPage(userQueryRequest);
    }

    @Override
    public List<User> listByIds(Set<Long> userIdSet) {
        return userDomainService.listByIds(userIdSet);
    }


}




