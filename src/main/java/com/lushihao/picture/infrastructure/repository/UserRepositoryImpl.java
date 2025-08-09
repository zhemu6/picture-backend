package com.lushihao.picture.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.domain.user.repository.UserRepository;
import com.lushihao.picture.infrastructure.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
 * 用户领域仓储的具体实现
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-08   16:05
 */
@Service
public class UserRepositoryImpl extends ServiceImpl<UserMapper, User> implements UserRepository {

}
