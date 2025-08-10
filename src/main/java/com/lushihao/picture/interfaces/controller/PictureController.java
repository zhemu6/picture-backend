package com.lushihao.picture.interfaces.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.lushihao.picture.application.service.PictureApplicationService;
import com.lushihao.picture.application.service.UserApplicationService;
import com.lushihao.picture.domain.picture.entity.Picture;
import com.lushihao.picture.domain.picture.valueobject.PictureReviewsStatusEnum;
import com.lushihao.picture.domain.user.constant.UserConstant;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.infrastructure.annotation.AuthCheck;
import com.lushihao.picture.infrastructure.api.aliyunai.AliYunAiApi;
import com.lushihao.picture.infrastructure.api.aliyunai.model.CreateTaskResponse;
import com.lushihao.picture.infrastructure.api.aliyunai.model.GetCommonSynthesisTaskResponse;
import com.lushihao.picture.infrastructure.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.lushihao.picture.infrastructure.common.BaseResponse;
import com.lushihao.picture.infrastructure.common.DeleteRequest;
import com.lushihao.picture.infrastructure.common.ResultUtils;
import com.lushihao.picture.infrastructure.exception.ErrorCode;
import com.lushihao.picture.infrastructure.exception.ThrowUtils;
import com.lushihao.picture.interfaces.assemble.PictureAssembler;
import com.lushihao.picture.interfaces.dto.picture.*;
import com.lushihao.picture.interfaces.vo.picture.PictureRankVO;
import com.lushihao.picture.interfaces.vo.picture.PictureTagCategory;
import com.lushihao.picture.interfaces.vo.picture.PictureVO;
import com.lushihao.picture.shared.auth.SpaceUserAuthManager;
import com.lushihao.picture.shared.auth.StpKit;
import com.lushihao.picture.shared.auth.annotation.SaSpaceCheckPermission;
import com.lushihao.picture.shared.auth.model.SpaceUserPermissionConstant;
import com.lushihao.picture.domain.space.entity.Space;
import com.lushihao.picture.application.service.SpaceApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
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
    private PictureApplicationService pictureApplicationService;
    @Resource
    private UserApplicationService userApplicationService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private SpaceApplicationService spaceApplicationService;
    @Resource
    private AliYunAiApi aliYunAiApi;
    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;


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
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile, PictureUploadRequest uploadRequest, HttpServletRequest request) {
        User loginUser = userApplicationService.getLoginUser(request);
        PictureVO pictureVO = pictureApplicationService.uploadPicture(multipartFile, uploadRequest, loginUser);
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
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadRequest uploadRequest, HttpServletRequest request) {
        User loginUser = userApplicationService.getLoginUser(request);
        String fileUrl = uploadRequest.getFileUrl();
        PictureVO pictureVO = pictureApplicationService.uploadPicture(fileUrl, uploadRequest, loginUser);
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
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_DELETE)
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        pictureApplicationService.deletePicture(deleteRequest, request);
        return ResultUtils.success(true);
    }


    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 将实体类和DTO转换
        Picture picture = PictureAssembler.toPictureEntity(pictureUpdateRequest);

        // 将list转为String
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        // 数据校验
        pictureApplicationService.validPicture(picture);
        // 判断是否存在
        long id = pictureUpdateRequest.getId();
        // 判断是否存在
        Picture oldPicture = pictureApplicationService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        User loginUser = userApplicationService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.PARAMS_ERROR);
        // 填入审核参数
        pictureApplicationService.fillReviewParams(oldPicture, loginUser);
        // 操作数据库
        boolean isUpdate = pictureApplicationService.updateById(picture);
        ThrowUtils.throwIf(!isUpdate, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureApplicationService.getById(id);
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
        Picture picture = pictureApplicationService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 空间权限校验
        Long spaceId = picture.getSpaceId();
        // 如果是私有空间 需要判断是否有权限
        Space space = null;
        if (spaceId != null) {
            boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR);
//            User loginUser = userService.getLoginUser(request);
            // 进行权限校验 现在通过框架实现 直接注释
//            pictureService.checkPictureAuth(loginUser, picture);
            space = spaceApplicationService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }
        PictureVO pictureVO = pictureApplicationService.getPictureVO(picture, request);
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, userApplicationService.getLoginUser(request));
        pictureVO.setPermissionList(permissionList);
        return ResultUtils.success(pictureVO);
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
        Page<Picture> picturePage = pictureApplicationService.page(new Page<>(current, size), pictureApplicationService.getLambdaQueryWrapper(pictureQueryRequest));
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
            // 我们需要查私有空间的
            boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR);
        }

        // 查询数据库
        Page<Picture> picturePage = pictureApplicationService.page(new Page<>(current, size), pictureApplicationService.getLambdaQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(pictureApplicationService.getPictureVOPage(picturePage, request));
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
        Page<Picture> picturePage = pictureApplicationService.page(new Page<>(current, size), pictureApplicationService.getLambdaQueryWrapper(pictureQueryRequest));
        Page<PictureVO> pictureVOPage = pictureApplicationService.getPictureVOPage(picturePage, request);
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
        Page<Picture> picturePage = pictureApplicationService.page(new Page<>(current, size), pictureApplicationService.getLambdaQueryWrapper(pictureQueryRequest));
        Page<PictureVO> pictureVOPage = pictureApplicationService.getPictureVOPage(picturePage, request);
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
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest editRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(editRequest == null || editRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userApplicationService.getLoginUser(request);
        // 将实体类和DTO转换
        Picture picture = PictureAssembler.toPictureEntity(editRequest);
        List<String> tags = editRequest.getTags();
        pictureApplicationService.editPicture(picture, tags, loginUser);
        return ResultUtils.success(true);
    }

    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList(
                // 📸 内容题材
                "人像", "风景", "城市", "建筑", "动物", "植物", "街拍", "夜景", "星空", "美食", "旅行", "人文", "儿童", "海边",

                // 🎨 风格视觉
                "黑白", "复古", "清新", "极简", "HDR", "赛博朋克", "文艺", "梦幻", "国风", "暗黑", "胶片感",

                // 🧠 情绪表达
                "浪漫", "治愈", "孤独", "温暖", "悲伤", "冷静", "希望", "宁静", "热烈", "自由",

                // 🛠 技法构图
                "特写", "虚化", "长曝光", "剪影", "光影", "构图巧妙", "对称", "低角度", "高角度", "慢门", "反射", "色彩对比",

                // 📷 设备品牌（合并进tag）
                "佳能", "尼康", "索尼", "富士", "松下", "徕卡", "奥林巴斯", "宾得",
                "小米", "华为", "苹果", "三星", "谷歌", "一加", "OPPO", "vivo"


        );

        List<String> categoryList = Arrays.asList(
                // 人物、写真、肖像
                "人像摄影",
                // 自然风光、山川河海
                "风光摄影",
                // 街景、人文、城市记录
                "城市街拍",
                // 动物、植物、生态
                "动植物",
                // 新闻、社会、现场纪实
                "纪实摄影",
                // 多重曝光、概念摄影
                "创意摄影",
                // 黑白风格作品
                "黑白摄影",
                // 夜晚、灯光、星空
                "夜景摄影",
                // 无人机、高空视角
                "航拍摄影",
                // 出游摄影、打卡风景
                "旅行摄影"
        );

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
        User loginUser = userApplicationService.getLoginUser(request);
        pictureApplicationService.doPictureReview(pictureReviewRequest, loginUser);
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
        User loginUser = userApplicationService.getLoginUser(request);
        Integer count = pictureApplicationService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(count);
    }

    /**
     * 根据颜色搜索图片
     *
     * @param searchPictureByColorRequest 空间id 图片颜色 16
     * @param request                     用户获取登录用户
     * @return 按照相似度 返回一个PictureVO列表
     */
    @PostMapping("/search/color")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResponse<List<PictureVO>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userApplicationService.getLoginUser(request);
        String picColor = searchPictureByColorRequest.getPicColor();
        Long spaceId = searchPictureByColorRequest.getSpaceId();
        List<PictureVO> pictureVOList = pictureApplicationService.searchPictureByColor(spaceId, picColor, loginUser);
        return ResultUtils.success(pictureVOList);
    }

    /**
     * 批量编辑图片
     */
    @PostMapping("/edit/batch")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPictureByBatch(@RequestBody PictureEditByBatchRequest editRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(editRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userApplicationService.getLoginUser(request);
        pictureApplicationService.editPictureByBatch(editRequest, loginUser);
        return ResultUtils.success(true);
    }


    @PostMapping("/out_painting/create_task")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<CreateTaskResponse> createPictureOutPaintingTask(@RequestBody CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(createPictureOutPaintingTaskRequest == null || createPictureOutPaintingTaskRequest.getPictureId() == null, ErrorCode.PARAMS_ERROR);
        CreateTaskResponse response = pictureApplicationService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest, request);
        return ResultUtils.success(response);
    }

    /**
     * 查询 AI 扩图任务
     */
    @GetMapping("/out_painting/get_task")
    public BaseResponse<GetOutPaintingTaskResponse> getOutPaintingPictureTask(String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
        GetOutPaintingTaskResponse task = aliYunAiApi.getOutPaintingTask(taskId);
        return ResultUtils.success(task);
    }

    @PostMapping("/common_synthesis/create_task")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<CreateTaskResponse> createPictureCommonSynthesisTask(@RequestBody CreatePictureCommonSynthesisTaskRequest createPictureCommonSynthesisTaskRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(createPictureCommonSynthesisTaskRequest == null || createPictureCommonSynthesisTaskRequest.getPictureId() == null, ErrorCode.PARAMS_ERROR);
        CreateTaskResponse response = pictureApplicationService.createPictureCommonSynthesisTask(createPictureCommonSynthesisTaskRequest, request);
        return ResultUtils.success(response);
    }

    /**
     * 查询 AI 扩图任务
     */
    @GetMapping("/common_synthesis/get_task")
    public BaseResponse<GetCommonSynthesisTaskResponse> getCommonSynthesisPictureTask(String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
        GetCommonSynthesisTaskResponse task = aliYunAiApi.getCommonSynthesisTask(taskId);
        return ResultUtils.success(task);
    }


    @GetMapping("/rank/{type}")
    public BaseResponse<List<PictureRankVO>> rankPicture(@PathVariable String type) {
        log.info(type);
        ThrowUtils.throwIf(!("day".equals(type) || "week".equals(type) || "month".equals(type)),ErrorCode.PARAMS_ERROR,"目前排行榜仅支持统计 日/周/月");
        List<PictureRankVO> pictureRankVOList = pictureApplicationService.getRankByType(type);
        return ResultUtils.success(pictureRankVOList);
    }

}


