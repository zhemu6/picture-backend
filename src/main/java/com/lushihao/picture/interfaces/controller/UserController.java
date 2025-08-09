package com.lushihao.picture.interfaces.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.picture.infrastructure.annotation.AuthCheck;
import com.lushihao.picture.infrastructure.common.BaseResponse;
import com.lushihao.picture.infrastructure.common.DeleteRequest;
import com.lushihao.picture.infrastructure.common.ResultUtils;
import com.lushihao.picture.interfaces.dto.user.*;
import com.lushihao.picture.domain.user.constant.UserConstant;
import com.lushihao.picture.infrastructure.exception.ErrorCode;
import com.lushihao.picture.infrastructure.exception.ThrowUtils;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.interfaces.vo.user.LoginUserVO;
import com.lushihao.picture.interfaces.vo.user.UserVO;
import com.lushihao.picture.application.service.PictureApplicationService;
import com.lushihao.picture.application.service.UserApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户管理的相关Controller
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-27   17:20
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserApplicationService userApplicationService;

    @Resource
    private PictureApplicationService pictureApplicationService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册封装类
     * @return 用户的注册id
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        log.info("用户注册功能，请求参数为：{}", userRegisterRequest);
        long userId = userApplicationService.userRegister(userRegisterRequest);
        return ResultUtils.success(userId);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登陆封装类
     * @return 后端给前端返回的封装类型
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        log.info("用户登录功能，请求参数为：{}", userLoginRequest);
        LoginUserVO loginUserVO = userApplicationService.userLogin(userLoginRequest, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取用户登录信息
     *
     * @param request request
     * @return 脱敏后的登录用户信息
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        log.info("获取用户登录信息，请求参数为");
        User user = userApplicationService.getLoginUser(request);
        return ResultUtils.success(userApplicationService.getLoginUserVO(user));
    }

    /**
     * 用户注销
     *
     * @param request request
     * @return 是否注销成功
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        log.info("用户注销功能，请求参数为：{}", request);
        boolean isLogout = userApplicationService.userLoginOut(request);
        return ResultUtils.success(isLogout);
    }

    /**
     * 管理员添加用户
     *
     * @param userAddRequest 用户添加封装类
     * @return 添加的用户id
     */

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        log.info("管理员创建用户功能，请求参数为：{}", userAddRequest);
        Long userId = userApplicationService.addUser(userAddRequest);
        return ResultUtils.success(userId);
    }

    /**
     * 管理员根据ID查询用户信息（不用脱敏）
     *
     * @param id 用户id
     * @return 查询到的用户信息
     */

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        log.info("管理员根据id查询用户，请求参数为：{}", id);
        User user = userApplicationService.getUserById(id);
        return ResultUtils.success(user);
    }

    /**
     * 根据ID查询用户VO类（脱敏）
     *
     * @param id 用户id
     * @return 查询到的用户信息VO 脱敏
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        log.info("根据id查询用户VO，请求参数为：{}", id);
        UserVO userVO = userApplicationService.getUserVOById(id);
        return ResultUtils.success(userVO);
    }

    /**
     * 管理员删除用户（逻辑删除）
     *
     * @param deleteRequest 删除的包装类
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUserById(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        log.info("管理员根据删除用户，请求参数为：{}", deleteRequest);
        Boolean isDelete = userApplicationService.deleteUserById(deleteRequest);
        return ResultUtils.success(isDelete);
    }

    /**
     * 管理员更新用户
     *
     * @param userUpdateRequest 更新用户包装类
     * @return 是否更新成功
     */

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        log.info("管理员更新用户，请求参数为：{}", userUpdateRequest);
        Boolean isUpdate = userApplicationService.updateUser(userUpdateRequest);
        return ResultUtils.success(isUpdate);
    }

    /**
     * 管理员分页获取用户列表 VO类
     *
     * @param userQueryRequest 用户查询类
     * @return UserVO列表
     */

    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        log.info("管理员分页获取用户列表，请求参数为：{}", userQueryRequest);
        Page<UserVO> userVOList = userApplicationService.listUserVOByPage(userQueryRequest);
        return ResultUtils.success(userVOList);
    }
    /**
     * 上传头像
     */
    @PostMapping("/upload/avatar")
    public BaseResponse<String> uploadAvatar(@RequestPart("file") MultipartFile multipartFile,HttpServletRequest request) {
        ThrowUtils.throwIf(multipartFile == null || multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        User loginUser = userApplicationService.getLoginUser(request);
        String avatarUrl = pictureApplicationService.uploadAvatar(multipartFile,loginUser);
        return ResultUtils.success(avatarUrl);
    }


}


