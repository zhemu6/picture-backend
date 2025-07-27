package com.lushihao.picturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.picturebackend.common.DeleteRequest;
import com.lushihao.picturebackend.exception.BusinessException;
import com.lushihao.picturebackend.exception.ErrorCode;
import com.lushihao.picturebackend.exception.ThrowUtils;
import com.lushihao.picturebackend.model.dto.user.*;
import com.lushihao.picturebackend.model.entity.User;
import com.lushihao.picturebackend.model.enums.UserRoleEnum;
import com.lushihao.picturebackend.model.vo.LoginUserVO;
import com.lushihao.picturebackend.model.vo.UserVO;
import com.lushihao.picturebackend.service.UserService;
import com.lushihao.picturebackend.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.lushihao.picturebackend.constant.UserConstant.USER_LOGIN_STATE;


/**
 * @author lushihao
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2025-07-27 16:35:43
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    /**
     * 用户注册功能
     * @param userRegisterRequest 用户注册封装
     * @return 新用户的id
     */
    @Override
    public long userRegister( UserRegisterRequest userRegisterRequest) {

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        // 1.参数校验
        // 1.1 所有参数不能为空
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount,userPassword,checkPassword),ErrorCode.PARAMS_ERROR,"用户名或密码不能为空");
        // 1.2 账号需要满足规范
        ThrowUtils.throwIf(userAccount.length()<4,ErrorCode.PARAMS_ERROR,"用户账号过短");
        // 1.3 两个密码相同
        ThrowUtils.throwIf(!userPassword.equals(checkPassword),ErrorCode.PARAMS_ERROR,"两次密码不相同");
        // 1.4 密码需要满足规范(此时两个密码已经相同 直接鉴定其中一个即可)
        ThrowUtils.throwIf(userPassword.length()<8,ErrorCode.PARAMS_ERROR,"用户密码过短");
        // 2.账号不能重复
        // 2.1 自己定义一个查询条件 这里由于实体类采用的是驼峰命名法而数据库中
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        // 查询userAccount值和传入的userAccount值相同
        queryWrapper.eq(User::getUserAccount,userAccount);
        // 2.2 利用这个userMapper的selectCount方法从数据库查询数量
        Long count = this.baseMapper.selectCount(queryWrapper);
        ThrowUtils.throwIf(count>0,ErrorCode.PARAMS_ERROR,"用户账号已存在");
        // 3.对密码进行加密 获得加密后的密码
        String encryptedPassword = getEncryptedPassword(userPassword);
        // 插入到数据库中
        User user = new User();
        user.setUserAccount(userAccount);
        // 生成一个8位的随机英文字母
        String randomSuffix  = RandomUtil.randomString(RandomUtil.BASE_CHAR, 8);
        user.setUserName("初始用户" + randomSuffix);
        user.setUserPassword(encryptedPassword);
        user.setUserRole(UserRoleEnum.User.getValue());
        boolean idSaved = this.save(user);
        // 如果注册失败
        if(!idSaved){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"用户注册失败、数据库异常");
        }

        return user.getId();
    }

    /**
     * 密码加密
     * @param password 原始密码
     * @return 加密密码
     */
    @Override
    public String getEncryptedPassword(String password) {
        // 盐值 md5加盐
        final String SALT = "ShihaoLu";
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes());
    }

    /**
     * 用户登录
     * @param userLoginRequest 用户登录封装类
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        // 1.用户登录 首先还是对账号密码进行校验
        // 1.1 首先不能为空
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount,userPassword),ErrorCode.PARAMS_ERROR,"用户名或密码不能为空");
        // 1.1 用户名大于4和密码都要大于8
        ThrowUtils.throwIf(userAccount.length()<4,ErrorCode.PARAMS_ERROR,"用户账号过短");
        ThrowUtils.throwIf(userPassword.length()<8,ErrorCode.PARAMS_ERROR,"用户密码过短");
        // 2.根据账号查询用户信息
        // 2.1 获得加密后的信息
        String encryptedPassword = getEncryptedPassword(userPassword);
        // 2.2 查询数据库
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount,userAccount);
        queryWrapper.eq(User::getUserPassword,encryptedPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 如果查到的用户为空
        ThrowUtils.throwIf(user == null,ErrorCode.PARAMS_ERROR,"用户或密码错误");
        // 3.记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 4.通过这个登录态 我们可以来获取当前登录用户
        return getLoginUserVO(user);
    }

    /**
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request){
        // 从Session中获得用户
        User currentUser = (User)request.getSession().getAttribute(USER_LOGIN_STATE);
        // 如果用户为空或者没有id 认为没登录
        ThrowUtils.throwIf(currentUser == null || currentUser.getId() == null,ErrorCode.NOT_LOGIN_ERROR);
        // 在根据这个信息从数据库中查询
        Long userId =  currentUser.getId();
        currentUser = this.getById(userId);
        // 比如用户自动注销了
        ThrowUtils.throwIf(currentUser == null,ErrorCode.NOT_LOGIN_ERROR);
        return currentUser;
    }

    /**
     * 根据用户 来获得转换后的LoginUserVO对象
     * @param user 用户
     * @return LoginUserVO
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user,loginUserVO);
        return loginUserVO;
    }

    /**
     * 用户注销
     * @param request 请求
     * @return 是否成功注销
     */
    @Override
    public boolean  userLoginOut(HttpServletRequest request) {
        // 从Session中获得用户
        User currentUser = (User)request.getSession().getAttribute(USER_LOGIN_STATE);
        // 如果用户为空或者没有id 认为没登录
        ThrowUtils.throwIf(currentUser == null || currentUser.getId() == null,ErrorCode.OPERATION_ERROR,"未登录");
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }
    /**
     * 获取单个用户的脱敏用户对象
     * @param user 用户
     * @return 脱敏用户对象
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user,userVO);
        return userVO;
    }

    /**
     * 活个多个用户对象的脱敏列表
     * @param userList 用户列表
     * @return 脱敏用户列表
     */
    @Override
    public List<UserVO> getUserV0List(List<User> userList) {
        if(CollUtil.isEmpty(userList)){
            return new ArrayList<>();
        }

        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 将用户的查询请求类转换成LambdaQueryWrapper对象
     * @param userQueryRequest 用户查询请求类
     * @return LambdaQueryWrapper对象
     */
    @Override
    public LambdaQueryWrapper<User> getLambdaQueryWrapper(UserQueryRequest userQueryRequest) {
        if(userQueryRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获得userQueryRequest中的相关值
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        // 定义一个新的LambdaQueryWrapper
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        // 如果是用户的ID或者是权限 我们需要精准的查询
        queryWrapper.eq(ObjUtil.isNotNull(id), User::getId, id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), User::getUserRole, userRole);
        // 用户的账户、用户名、个人简介 我们可以使用模糊查询
        queryWrapper.like(StrUtil.isNotBlank(userAccount), User::getUserAccount, userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), User::getUserName, userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), User::getUserProfile, userProfile);

        // 排序处理
        if (StrUtil.isNotBlank(sortField)) {
            boolean isAsc = "ascend".equalsIgnoreCase(sortOrder);

            switch (sortField) {
                case "id":
                    queryWrapper.orderBy(true, isAsc, User::getId);
                    break;
                case "userName":
                    queryWrapper.orderBy(true, isAsc, User::getUserName);
                    break;
                case "userAccount":
                    queryWrapper.orderBy(true, isAsc, User::getUserAccount);
                    break;
                case "userProfile":
                    queryWrapper.orderBy(true, isAsc, User::getUserProfile);
                    break;
                case "createTime":
                    queryWrapper.orderBy(true, isAsc, User::getCreateTime);
                    break;
                default:
                    // 忽略未知字段或抛异常也可
                    break;
            }
        }


        return queryWrapper;
    }

    /**
     * 管理员创建单个用户
     * @param userAddRequest 用户请求类
     * @return 新用户的id
     */
    @Override
    public Long addUser(UserAddRequest userAddRequest) {
        String userAccount = userAddRequest.getUserAccount();
        ThrowUtils.throwIf(userAccount == null, ErrorCode.PARAMS_ERROR);

        // 自己定义一个查询条件 这里由于实体类采用的是驼峰命名法而数据库中
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        // 查询userAccount值和传入的userAccount值相同
        queryWrapper.eq(User::getUserAccount,userAccount);
        // 2.2 利用这个userMapper的selectCount方法从数据库查询数量
        Long count = this.baseMapper.selectCount(queryWrapper);
        ThrowUtils.throwIf(count>0,ErrorCode.PARAMS_ERROR,"用户账号已存在");

        User user = new User();
        BeanUtil.copyProperties(userAddRequest,user);
        // 设置默认密码
        final String DEFAULT_PASSWORD = "12345678";
        String encryptedPassword = getEncryptedPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptedPassword);
        boolean isAdd = this.save(user);
        ThrowUtils.throwIf(!isAdd, ErrorCode.OPERATION_ERROR);
        return user.getId();
    }

    /**
     * 管理员根据ID查询用户信息（不用脱敏）
     * @param id 用户id
     * @return 查询到的用户信息
     */
    @Override
    public User getUserById(Long id) {
        ThrowUtils.throwIf(id<0, ErrorCode.PARAMS_ERROR);
        User user = this.getById(id);
        ThrowUtils.throwIf(user==null, ErrorCode.NOT_FOUND_ERROR);
        return user;
    }

    /**
     * 根据ID查询用户VO类（脱敏）
     * @param id 用户id
     * @return 查询到的用户信息VO 脱敏
     */
    @Override
    public UserVO getUserVOById(Long id) {
        User user = this.getUserById(id);
        return this.getUserVO(user);
    }

    /**
     * 管理员删除用户
     * @param deleteRequest 删除的包装类
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteUserById(DeleteRequest deleteRequest) {
        Long id  = deleteRequest.getId();
        ThrowUtils.throwIf(id<=0,ErrorCode.PARAMS_ERROR);
        Boolean isDelete =  this.removeById(id);
        // 如果isDelete为0 代表没有删除掉数据 报错
        ThrowUtils.throwIf(!isDelete,ErrorCode.OPERATION_ERROR);
        return isDelete;
    }

    /**
     * 管理员更新用户
     * @param userUpdateRequest 更新用户包装类
     * @return 是否更新成功
     */
    @Override
    public Boolean updateUser(UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest.getId()==null,ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest,user);
        boolean idUpdate = this.updateById(user);
        // TODO 实际上这里无论是否有更新 他都会返回True 后期考虑修改
        ThrowUtils.throwIf(!idUpdate,ErrorCode.OPERATION_ERROR);
        return idUpdate;
    }


    /**
     * 管理员分页获取用户列表 VO类
     * @param userQueryRequest 用户查询类
     * @return UserVO列表
     */
    @Override
    public Page<UserVO>  listUserVOByPage(UserQueryRequest userQueryRequest) {
        // 获取当前的分页号和页面大小
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        // 首先查询分页后的用户列表
        // 构造分页参数对象 要查第几页 每页多少条
        Page<User> pagePara = new Page<>(current,pageSize);
        // 构造查询条件
        LambdaQueryWrapper<User> lambdaQueryWrapper = this.getLambdaQueryWrapper(userQueryRequest);
        // 查询分页后的用户列表
        Page<User> userPage = this.page(pagePara,lambdaQueryWrapper);
        // 创建一个分页对象 用于封装最后的UserVO类型
        Page<UserVO> userVOPage = new Page<>(current,pageSize,userPage.getTotal());
        // User 实体列表转换成 UserVO 列表
        List<UserVO> userVOList =  this.getUserV0List(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return userVOPage;
    }


}




