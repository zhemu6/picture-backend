package com.lushihao.picture.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lushihao.picture.domain.picture.entity.Picture;
import com.lushihao.picture.interfaces.dto.space.SpaceAddRequest;
import com.lushihao.picture.interfaces.dto.space.SpaceQueryRequest;
import com.lushihao.picture.domain.space.entity.Space;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.interfaces.vo.space.SpaceVO;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author lushihao
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-08-02 20:29:16
 */
public interface SpaceApplicationService {


    /**
     * 根据查询条件古剑LambdaQueryWrapper对象
     *
     * @param spaceQueryRequest 查询条件
     * @return LambdaQueryWrapper对象
     */
    LambdaQueryWrapper<Space> getLambdaQueryWrapper(SpaceQueryRequest spaceQueryRequest);


    /**
     * 获取空间VO类
     *
     * @param space   传入一个space
     * @param request 请求
     * @return SpaceVO类
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 分页获取图片VO类
     *
     * @param spacePage 分页对象
     * @param request   请求
     * @return 分页VO对象
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 传入一个空间对象 根据这个空间对象的level 自动分配max-size 和max-count
     *
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);


    /**
     * 空间创建
     *
     * @param space     空间常见请求
     * @param loginUser 获取登录用户
     * @return 创建空间的id
     */
    long addSpace(Space space,Integer spaceType, User loginUser);


    void checkSpaceAuth(Space space, User loginUser);

    boolean save(Space space);

    void deleteSpace(Long spaceId,User loginUser);

    boolean removeById(Long spaceId);

    Space getById(Long spaceId);

    boolean updateById(Space space);


    Page<Space> page(Page<Space> spacePage, LambdaQueryWrapper<Space> lambdaQueryWrapper);

    List<Space> listByIds(Set<Long> spaceIdSet);

    boolean addUpdatePicture(Long finalSpaceId, Long picSize);

    boolean deleteUpdatePicture(Long spaceId, Long picSize);

    List<Space> list(QueryWrapper<Space> queryWrapper);

    Set<Long> getSpaceId(int value);
}
