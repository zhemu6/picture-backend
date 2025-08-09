package com.lushihao.picture.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lushihao.picture.domain.space.entity.SpaceUser;
import com.lushihao.picture.interfaces.dto.spaceuser.SpaceUserQueryRequest;
import com.lushihao.picture.interfaces.vo.space.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author lushihao
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service
 * @createDate 2025-08-06 14:29:25
 */
public interface SpaceUserApplicationService {

    LambdaQueryWrapper<SpaceUser> getLambdaQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    void validSpaceUser(SpaceUser spaceUser, boolean add);

    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);

    long addSpaceUser(SpaceUser spaceUser);

    boolean save(SpaceUser spaceUser);

    void deleteSpaceUser(long spaceId);

    SpaceUser getOne(LambdaQueryWrapper<SpaceUser> lambdaQueryWrapper);

    List<SpaceUser> list(LambdaQueryWrapper<SpaceUser> lambdaQueryWrapper);

    SpaceUser getById(long id);

    boolean updateById(SpaceUser spaceUser);

    SpaceUser getSpaceUserBySpaceIdAndUserId(Long spaceId, Long userId);
}
