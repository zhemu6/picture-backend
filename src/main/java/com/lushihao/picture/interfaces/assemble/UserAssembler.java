package com.lushihao.picture.interfaces.assemble;

import cn.hutool.core.bean.BeanUtil;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.interfaces.dto.user.UserAddRequest;
import com.lushihao.picture.interfaces.dto.user.UserUpdateRequest;

/**
 * 用户对象转换
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-08   17:12
 */
public class UserAssembler {
    /**
     * 用户添加请求转换成实体类
     * @param userAddRequest 用户添加请求
     * @return 用户实体类
     */
    public static User toUserEntity(UserAddRequest userAddRequest){
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        return user;
    }
    /**
     * 用户更新请求转换成实体类
     * @param userUpdateRequest 用户更新请求
     * @return 用户实体类
     */
    public static User toUserEntity(UserUpdateRequest userUpdateRequest){
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        return user;
    }

}
