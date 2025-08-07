package com.lushihao.picturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lushihao.picturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.lushihao.picturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.lushihao.picturebackend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lushihao.picturebackend.model.vo.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author lushihao
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service
 * @createDate 2025-08-06 14:29:25
 */
public interface SpaceUserService extends IService<SpaceUser> {

    LambdaQueryWrapper<SpaceUser> getLambdaQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    void validSpaceUser(SpaceUser spaceUser, boolean add);

    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);

    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);
}
