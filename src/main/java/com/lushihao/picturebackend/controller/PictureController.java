package com.lushihao.picturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.lushihao.picturebackend.annotation.AuthCheck;
import com.lushihao.picturebackend.common.BaseResponse;
import com.lushihao.picturebackend.common.DeleteRequest;
import com.lushihao.picturebackend.common.ResultUtils;
import com.lushihao.picturebackend.constant.PictureTagCategory;
import com.lushihao.picturebackend.constant.UserConstant;
import com.lushihao.picturebackend.exception.ErrorCode;
import com.lushihao.picturebackend.exception.ThrowUtils;
import com.lushihao.picturebackend.model.dto.picture.*;
import com.lushihao.picturebackend.model.dto.user.*;
import com.lushihao.picturebackend.model.entity.Picture;
import com.lushihao.picturebackend.model.entity.Space;
import com.lushihao.picturebackend.model.entity.User;
import com.lushihao.picturebackend.model.enums.PictureReviewsStatusEnum;
import com.lushihao.picturebackend.model.vo.LoginUserVO;
import com.lushihao.picturebackend.model.vo.PictureVO;
import com.lushihao.picturebackend.model.vo.UserVO;
import com.lushihao.picturebackend.service.PictureService;
import com.lushihao.picturebackend.service.SpaceService;
import com.lushihao.picturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 图片管理的相关Controller
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-30   11:16
 */
@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private PictureService pictureService;
    @Resource
    private UserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private SpaceService spaceService;
    /**
     * 构造本地缓存
     */
    private final Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder()
                    // 初始容量
                    .initialCapacity(1024)
                    // 最大存储10000条数据
                    .maximumSize(10000L)
                    // 缓存 5 分钟移除
                    .expireAfterWrite(5L, TimeUnit.MINUTES)
                    .build();


    /**
     * 图片上传功能
     *
     * @param multipartFile 文件
     * @param uploadRequest 上传请求
     * @param request       请求
     * @return 返回图片封装类型VO
     */
    @PostMapping("/upload")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile, PictureUploadRequest uploadRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, uploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 通过URL图片上传功能
     *
     * @param uploadRequest 上传请求
     * @param request       请求
     * @return 返回图片封装类型VO
     */
    @PostMapping("/upload/url")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadRequest uploadRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = uploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, uploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }


    /**
     * 删除 图片
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        pictureService.deletePicture(deleteRequest, request);
        return ResultUtils.success(true);
    }


    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 将实体类和DTO转换
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUpdateRequest, picture);
        // 将list转为String
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        // 数据校验
        pictureService.validPicture(picture);
        // 判断是否存在
        long id = pictureUpdateRequest.getId();
        // 判断是否存在
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.PARAMS_ERROR);
        // 填入审核参数
        pictureService.fillReviewParams(oldPicture, loginUser);
        // 操作数据库
        boolean isUpdate = pictureService.updateById(picture);
        ThrowUtils.throwIf(!isUpdate, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);


        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(picture);
    }

    /**
     * 根据 id 获取图片（封装类）
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 空间权限校验
        Long spaceId = picture.getSpaceId();
        // 如果是私有空间 需要判断是否有权限
        if (spaceId != null) {
            User loginUser = userService.getLoginUser(request);
            // 进行权限校验
            pictureService.checkPictureAuth(loginUser, picture);
        }

        return ResultUtils.success(pictureService.getPictureVO(picture, request));
    }

    /**
     * 分页获取图片列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        //查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getLambdaQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表（封装类）
     * 提供给普通用户
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        // 空间权限校验
        Long spaceId = pictureQueryRequest.getSpaceId();
        if (spaceId == null) {
            // 如果是公开图库 则我们限制用户只能看到审核通过的
            pictureQueryRequest.setReviewStatus(PictureReviewsStatusEnum.Pass.getValue());
            // 并且我们设置nullSpaceId为true
            pictureQueryRequest.setNullSpaceId(true);
        } else {
            // 如果是私有空间
            User loginUser = userService.getLoginUser(request);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // 如果空间存在 则只有空间管理员可以使用
            ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "只有空间管理员可以使用");
        }

        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getLambdaQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }

    /**
     * 分页获取图片列表（封装类，使用Caffeine 本地缓存 + Redis 分布式缓存）
     * 提供给普通用户
     */
    @Deprecated
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 限制用户只能看到审核通过的
        pictureQueryRequest.setReviewStatus(PictureReviewsStatusEnum.Pass.getValue());
        // 查询数据库之前 可以先在缓存中查询 看其中是否存在
        // 1. 构建缓存key
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey = String.format("picture:listPictureVOByPage:%s", hashKey);
        // 2.先查本地缓存
        String cachedValue = LOCAL_CACHE.getIfPresent(cacheKey);
        if (StrUtil.isNotBlank(cachedValue)) {
            // 缓存命中 返回缓存结果（反序列化）
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            ResultUtils.success(cachedPage);
        }
        // 3.本地缓存没有命中 查询redis分布式缓存
        // 操作redis 从缓存中查询
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        cachedValue = opsForValue.get(cacheKey);
        if (StrUtil.isNotBlank(cachedValue)) {
            // 存入本地缓存
            LOCAL_CACHE.put(cacheKey, cachedValue);
            // 缓存命中 返回缓存结果（反序列化）
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            ResultUtils.success(cachedPage);
        }
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getLambdaQueryWrapper(pictureQueryRequest));
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        // 设置缓存过期时间 5-10 min 防止缓存血崩
        int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300);
        opsForValue.set(cacheKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);
        LOCAL_CACHE.put(cacheKey, cacheValue);
        // 将查询到的存入redis中
        return ResultUtils.success(pictureVOPage);
    }


    /**
     * 分页获取图片列表（封装类，使用redis缓存）
     * 提供给普通用户
     */
    @PostMapping("/list/page/vo/localCache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithLocalCache(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 限制用户只能看到审核通过的
        pictureQueryRequest.setReviewStatus(PictureReviewsStatusEnum.Pass.getValue());
        // 查询数据库之前 可以先在缓存中查询 看其中是否存在
        // 1. 构建缓存key
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey = String.format("listPictureVOByPage:%s", hashKey);
        // 操作redis 从缓存中查询
        String cachedValue = LOCAL_CACHE.getIfPresent(cacheKey);
        // 如果返回的不是空 就是之前有缓存 直接返回
        if (StrUtil.isNotBlank(cachedValue)) {
            // 缓存命中 返回缓存结果（反序列化）
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            ResultUtils.success(cachedPage);
        }
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getLambdaQueryWrapper(pictureQueryRequest));
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        // 设置缓存过期时间 5-10 min 防止缓存血崩
        LOCAL_CACHE.put(cacheKey, cacheValue);
        // 将查询到的存入redis中
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 编辑图片
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest editRequest, HttpServletRequest request) {
        pictureService.editPicture(editRequest, request);
        return ResultUtils.success(true);
    }

    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }


    /**
     * 审核图片 （仅管理员可用）PictureReviewRequest pictureReviewRequest,User loginUser
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.NOT_FOUND_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 批量上传抓取图片
     *
     * @param pictureUploadByBatchRequest 批量抓取请求参数
     * @param request                     请求
     * @return 抓取成功数量
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.NOT_FOUND_ERROR);
        User loginUser = userService.getLoginUser(request);
        Integer count = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(count);
    }


}


