package com.lushihao.picturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lushihao.picturebackend.model.dto.space.SpaceAddRequest;
import com.lushihao.picturebackend.model.dto.space.SpaceQueryRequest;
import com.lushihao.picturebackend.model.entity.Space;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lushihao.picturebackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author lushihao
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-08-02 20:29:16
 */
public interface SpaceService extends IService<Space> {


    /**
     * 根据查询条件古剑LambdaQueryWrapper对象
     *
     * @param spaceQueryRequest 查询条件
     * @return LambdaQueryWrapper对象
     */
    LambdaQueryWrapper<Space> getLambdaQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 空间校验
     *
     * @param space
     * @param add 是否是创建时创建
     */
    void validSpace(Space space,boolean add);

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
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);


    /**
     * 空间创建
     * @param spaceAddRequest 空间常见请求
     * @param request 获取登录用户
     * @return 创建空间的id
     */
    long addSpace(SpaceAddRequest spaceAddRequest,HttpServletRequest request);
}
