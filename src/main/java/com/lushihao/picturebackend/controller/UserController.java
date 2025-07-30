package com.lushihao.picturebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.picturebackend.annotation.AuthCheck;
import com.lushihao.picturebackend.common.BaseResponse;
import com.lushihao.picturebackend.common.DeleteRequest;
import com.lushihao.picturebackend.common.ResultUtils;
import com.lushihao.picturebackend.constant.UserConstant;
import com.lushihao.picturebackend.exception.ErrorCode;
import com.lushihao.picturebackend.exception.ThrowUtils;
import com.lushihao.picturebackend.model.dto.user.*;
import com.lushihao.picturebackend.model.entity.User;
import com.lushihao.picturebackend.model.enums.UserRoleEnum;
import com.lushihao.picturebackend.model.vo.LoginUserVO;
import com.lushihao.picturebackend.model.vo.UserVO;
import com.lushihao.picturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户管理的相关Controller
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-27   17:20
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     * @param userRegisterRequest 用户注册封装类
     * @return 用户的注册id
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        log.info("用户注册功能，请求参数为：{}", userRegisterRequest);
        long userId = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(userId);
    }

    /**
     * 用户登录
     * @param userLoginRequest 用户登陆封装类
     * @return 后端给前端返回的封装类型
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest ,HttpServletRequest request){
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        log.info("用户登录功能，请求参数为：{}", userLoginRequest);
        LoginUserVO loginUserVO = userService.userLogin(userLoginRequest,request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取用户登录信息
     * @param request request
     * @return 脱敏后的登录用户信息
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request){
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        log.info("获取用户登录信息，请求参数为");
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    /**
     * 用户注销
     * @param request request
     * @return 是否注销成功
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request){
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        log.info("用户注销功能，请求参数为：{}",request);
        boolean isLogout = userService.userLoginOut(request);
        return ResultUtils.success(isLogout);
    }

    /**
     * 管理员添加用户
     * @param userAddRequest 用户添加封装类
     * @return 添加的用户id
     */

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(UserAddRequest userAddRequest){
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        log.info("管理员创建用户功能，请求参数为：{}",userAddRequest);
        Long userId = userService.addUser(userAddRequest);
        return ResultUtils.success(userId);
    }

    /**
     * 管理员根据ID查询用户信息（不用脱敏）
     * @param id 用户id
     * @return 查询到的用户信息
     */

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(Long id){
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        log.info("管理员根据id查询用户，请求参数为：{}",id);
        User user = userService.getUserById(id);
        return ResultUtils.success(user);
    }

    /**
     * 根据ID查询用户VO类（脱敏）
     * @param id 用户id
     * @return 查询到的用户信息VO 脱敏
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(Long id){
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        log.info("根据id查询用户VO，请求参数为：{}",id);
        UserVO userVO = userService.getUserVOById(id);
        return ResultUtils.success(userVO);
    }

    /**
     * 管理员删除用户（逻辑删除）
     * @param deleteRequest 删除的包装类
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUserById(@RequestBody DeleteRequest deleteRequest){
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        log.info("管理员根据删除用户，请求参数为：{}",deleteRequest);
        Boolean isDelete = userService.deleteUserById(deleteRequest);
        return ResultUtils.success(isDelete);
    }

    /**
     * 管理员更新用户
     * @param userUpdateRequest 更新用户包装类
     * @return 是否更新成功
     */

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest){
        ThrowUtils.throwIf(userUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        log.info("管理员更新用户，请求参数为：{}",userUpdateRequest);
        Boolean isUpdate = userService.updateUser(userUpdateRequest);
        return ResultUtils.success(isUpdate);
    }

    /**
     * 管理员分页获取用户列表 VO类
     * @param userQueryRequest 用户查询类
     * @return UserVO列表
     */

    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest){
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        log.info("管理员分页获取用户列表，请求参数为：{}",userQueryRequest);
        Page<UserVO> userVOList = userService.listUserVOByPage(userQueryRequest);
        return ResultUtils.success(userVOList);
    }


}


