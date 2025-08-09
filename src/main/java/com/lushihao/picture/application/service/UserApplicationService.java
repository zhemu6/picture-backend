package com.lushihao.picture.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.picture.infrastructure.common.DeleteRequest;
import com.lushihao.picture.interfaces.dto.user.*;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.interfaces.vo.user.LoginUserVO;
import com.lushihao.picture.interfaces.vo.user.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
* @author lushihao
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-07-27 16:35:43
*/
public interface UserApplicationService{
    /**
     * 用户注册功能
     * @param userRegisterRequest 用户注册封装
     * @return 新用户的id
     */
    long userRegister( UserRegisterRequest userRegisterRequest);

    /**
     * 密码加密
     * @param password 原始密码
     * @return 加密密码
     */
    String getEncryptedPassword(String password);

    LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request);

    /**
     * 获取当前登录用户
     * @param request request
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    LoginUserVO getLoginUserVO(User user);

    boolean  userLoginOut(HttpServletRequest request);

    /**
     * 获取单个用户的脱敏用户对象
     * @param user 用户
     * @return 脱敏用户对象
     */
    public UserVO getUserVO(User user);

    /**
     * 活个多个用户对象的脱敏列表
     * @param userList 用户列表
     * @return 脱敏用户列表
     */
    public List<UserVO> getUserV0List(List<User> userList);

    /**
     * 将用户的查询请求类转换成LambdaQueryWrapper对象
     * @param userQueryRequest 用户查询请求类
     * @return LambdaQueryWrapper对象
     */
    public LambdaQueryWrapper<User> getLambdaQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 管理员创建单个用户
     * @param userAddRequest 用户请求类
     * @return 新用户的id
     */
    Long addUser(UserAddRequest userAddRequest);

    /**
     * 管理员根据ID查询用户信息（不用脱敏）
     * @param id 用户id
     * @return 查询到的用户信息
     */
    User getUserById(Long id);

    /**
     * 根据ID查询用户VO类（脱敏）
     * @param id 用户id
     * @return 查询到的用户信息VO 脱敏
     */
    UserVO getUserVOById(Long id);

    /**
     * 管理员删除用户
     * @param deleteRequest 删除的包装类
     * @return 是否删除成功
     */
    Boolean deleteUserById(DeleteRequest deleteRequest);

    /**
     * 管理员更新用户
     * @param userUpdateRequest 更新用户包装类
     * @return 是否更新成功
     */
    Boolean updateUser(UserUpdateRequest userUpdateRequest);

    /**
     * 管理员分页获取用户列表 VO类
     * @param userQueryRequest 用户查询类
     * @return UserVO page
     */
    Page<UserVO> listUserVOByPage(UserQueryRequest userQueryRequest);


    List<User> listByIds(Set<Long> userIdSet);


}

