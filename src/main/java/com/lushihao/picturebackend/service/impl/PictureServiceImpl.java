package com.lushihao.picturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.picturebackend.common.DeleteRequest;
import com.lushihao.picturebackend.exception.BusinessException;
import com.lushihao.picturebackend.exception.ErrorCode;
import com.lushihao.picturebackend.exception.ThrowUtils;
import com.lushihao.picturebackend.manager.CosManager;
import com.lushihao.picturebackend.manager.FileManager;
import com.lushihao.picturebackend.manager.upload.FilePictureUpload;
import com.lushihao.picturebackend.manager.upload.PictureUploadTemplate;
import com.lushihao.picturebackend.manager.upload.UrlPictureUpload;
import com.lushihao.picturebackend.model.dto.file.UploadPictureResult;
import com.lushihao.picturebackend.model.dto.picture.*;
import com.lushihao.picturebackend.model.entity.Picture;
import com.lushihao.picturebackend.model.entity.Space;
import com.lushihao.picturebackend.model.entity.User;
import com.lushihao.picturebackend.model.enums.PictureReviewsStatusEnum;
import com.lushihao.picturebackend.model.vo.PictureVO;
import com.lushihao.picturebackend.model.vo.UserVO;
import com.lushihao.picturebackend.service.*;
import com.lushihao.picturebackend.mapper.PictureMapper;
import com.lushihao.picturebackend.util.ColorSimilarUtils;
import com.lushihao.picturebackend.util.SSLUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.transaction.Transaction;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lushihao
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-07-30 09:56:20
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Resource
    private FileManager fileManager;

    @Resource
    private UserService userService;
    @Resource
    private PictureLikeService pictureLikeService;
    @Resource
    private PictureFavoriteService pictureFavoriteService;
    @Resource
    private FilePictureUpload filePictureUpload;
    @Resource
    private UrlPictureUpload urlPictureUpload;
    @Autowired
    private CosManager cosManager;
    @Resource
    private SpaceService spaceService;
    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 上传图片
     *
     * @param inputSource          文件
     * @param pictureUploadRequest 前端发来的请求
     * @param loginUser            登录用户
     * @return PictureVO
     */
    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // 校验用户是否登录
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 获取空间是否存在
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // 校验用户是否有权限 仅空间的创建者才可以上传图片到空间中
            ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "没有权限");
            // 校验空间额度是否足够
            ThrowUtils.throwIf(space.getTotalCount() >= space.getMaxCount(), ErrorCode.OPERATION_ERROR, "空间条数不足");
            ThrowUtils.throwIf(space.getTotalSize() >= space.getMaxSize(), ErrorCode.OPERATION_ERROR, "空间大小不足");
        }
        // 校验用户是否有权限


        // 用于判断是新增还是更新图片
        Long pictureId = null;
        // 获取图片的id
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        // 如果id存在 就是更新图片 需要校验图片是否存在
        if (pictureId != null) {
            // 更新图片
            // 获取旧的图片
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 仅本人或管理员可以编辑图片
            ThrowUtils.throwIf(!Objects.equals(loginUser.getId(), oldPicture.getId()) && !userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "仅本人或管理员可以编辑图片");
            // 在更新图片的这部分 我们需要校验新老空间id 是否相同
            // 没传spaceId 直接复用原有的
            if (spaceId == null) {
                // 如果老图的空间不为空 新的为空
                if (oldPicture.getSpaceId() != null) {
                    spaceId = oldPicture.getSpaceId();
                }
            } else {
                // 用户传入了spaceID 我们需要确保新老的spaceID为同一个
                ThrowUtils.throwIf(ObjUtil.notEqual(spaceId, oldPicture.getSpaceId()), ErrorCode.PARAMS_ERROR, "空间ID不一致");
            }
            this.clearPictureFile(oldPicture);
        }
        // 上传图片
        // 按照用户的id划分目录 同时进一步 按照空间划分目录
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        // 如果用户传入spaceId为空 存入公共目录
        if (spaceId != null) {
            uploadPathPrefix = String.format("space/%s", spaceId);
        }

        // 根据inputSource的类型判断是文件还是url来调用相应的接口
        // 默认是文件上传
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        // 如果inputSource是url 则修改pictureUploadTemplate为urlPictureUpload
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        // 构造要入库的图片信息
        Picture picture = new Picture();
        picture.setSpaceId(spaceId);
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        // 支持图片自定义名称
        String picName = uploadPictureResult.getPicName();
        String category = null;
        String tags = null;

        if (pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getFileName())) {
            picName = pictureUploadRequest.getFileName();
        }

        if (pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getCategory())) {
            category = pictureUploadRequest.getCategory();
        }
        if (pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getTags())) {
            tags = pictureUploadRequest.getTags();
        }

        picture.setTags(tags);
        picture.setCategory(category);
        picture.setName(picName);
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());

        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        picture.setCameraModel(uploadPictureResult.getCameraModel());
        picture.setLensModel(uploadPictureResult.getLensModel());
        picture.setFNumber(uploadPictureResult.getFNumber());
        picture.setIso(uploadPictureResult.getIso());
        picture.setExposureTime(uploadPictureResult.getExposureTime());
        picture.setFocalLength(uploadPictureResult.getFocalLength());
        picture.setTakenTime(uploadPictureResult.getTakenTime());
//        picture.setPicColor((uploadPictureResult.getPicColor()));
        picture.setPicColor(ColorSimilarUtils.expandHexColor(uploadPictureResult.getPicColor()));
        // 填入审核参数
        this.fillReviewParams(picture, loginUser);
        // 如果pictureId不为空 则为更新图片
        if (pictureId != null) {
            // 更新需要补充图片id 和编辑时间
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        // 通过编程式事务管理
        Long finalSpaceId = spaceId;

        transactionTemplate.execute(status -> {
            boolean result = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
            if (finalSpaceId != null) {
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, finalSpaceId)
                        .setSql("total_size = total_size + " + picture.getPicSize())
                        .setSql("total_count = total_count + 1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "额度更新失败");
            }
            return picture;
        });
        return PictureVO.objToVo(picture);
    }

    /**
     * 上传头像
     *
     * @param fileObj   头像文件
     * @param loginUser 登录用户
     * @return PictureVO
     */
    @Override
    public String uploadAvatar(MultipartFile fileObj, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        ThrowUtils.throwIf(fileObj == null, ErrorCode.NO_AUTH_ERROR, "上传文件格式错误");
        String uploadPathPrefix = String.format("public/%s/avatar", loginUser.getId());
        return filePictureUpload.uploadAvatar(fileObj, uploadPathPrefix);
    }


    /**
     * 获取查询条件 根据用户传入的参数 构造SQL查询
     *
     * @param pictureQueryRequest 前端传入查询的参数
     * @return 查询到的对象
     */
    @Override
    public LambdaQueryWrapper<Picture> getLambdaQueryWrapper(PictureQueryRequest pictureQueryRequest) {

        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String cameraModel = pictureQueryRequest.getCameraModel();
        String lensModel = pictureQueryRequest.getLensModel();
        Double fNumber = pictureQueryRequest.getFNumber();
        Integer iso = pictureQueryRequest.getIso();
        String exposureTime = pictureQueryRequest.getExposureTime();
        Double focalLength = pictureQueryRequest.getFocalLength();
        Date takenTime = pictureQueryRequest.getTakenTime();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        Date reviewTime = pictureQueryRequest.getReviewTime();
        Long spaceId = pictureQueryRequest.getSpaceId();
        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();
        Date startEditTime = pictureQueryRequest.getStartEditTime();
        Date endEditTime = pictureQueryRequest.getEndEditTime();

        // 定义一个新的LambdaQueryWrapper
        LambdaQueryWrapper<Picture> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(searchText)) {
            lambdaQueryWrapper.and(qw -> qw.like(Picture::getName, searchText).or().like(Picture::getIntroduction, searchText));
        }
        // 如果是用户的ID或者是权限 我们需要精准的查询
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(id), Picture::getId, id);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(userId), Picture::getUserId, userId);

        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(spaceId), Picture::getSpaceId, spaceId);
        lambdaQueryWrapper.isNull(nullSpaceId, Picture::getSpaceId);

        lambdaQueryWrapper.like(StrUtil.isNotBlank(name), Picture::getName, name);
        lambdaQueryWrapper.like(StrUtil.isNotBlank(introduction), Picture::getIntroduction, introduction);
        lambdaQueryWrapper.like(StrUtil.isNotBlank(picFormat), Picture::getPicFormat, picFormat);
        lambdaQueryWrapper.eq(StrUtil.isNotBlank(category), Picture::getCategory, category);

        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(picWidth), Picture::getPicWidth, picWidth);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(picHeight), Picture::getPicHeight, picHeight);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(picSize), Picture::getPicSize, picSize);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(picScale), Picture::getPicScale, picScale);

        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(cameraModel), Picture::getCameraModel, cameraModel);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(lensModel), Picture::getLensModel, lensModel);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(fNumber), Picture::getFNumber, fNumber);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(iso), Picture::getIso, iso);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(focalLength), Picture::getFocalLength, focalLength);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(exposureTime), Picture::getExposureTime, exposureTime);

        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), Picture::getReviewStatus, reviewStatus);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), Picture::getReviewerId, reviewerId);
        lambdaQueryWrapper.like(ObjUtil.isNotEmpty(reviewMessage), Picture::getReviewMessage, reviewMessage);

        // 搜索时间实现
        // >=开始时间
        lambdaQueryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), Picture::getEditTime, startEditTime);
        // <结束时间
        lambdaQueryWrapper.lt(ObjUtil.isNotEmpty(endEditTime), Picture::getEditTime, endEditTime);
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                lambdaQueryWrapper.like(Picture::getTags, "\"" + tag + "\"");
            }
        }


        final Map<String, SFunction<Picture, ?>> sortFieldMap;
        Map<String, SFunction<Picture, ?>> map = new HashMap<>();
        map.put("id", Picture::getId);
        map.put("name", Picture::getName);
        map.put("introduction", Picture::getIntroduction);
        map.put("category", Picture::getCategory);
        map.put("picSize", Picture::getPicSize);
        map.put("picWidth", Picture::getPicWidth);
        map.put("picHeight", Picture::getPicHeight);
        map.put("picScale", Picture::getPicScale);
        map.put("spaceId", Picture::getSpaceId);
        map.put("picFormat", Picture::getPicFormat);
        map.put("cameraModel", Picture::getCameraModel);
        map.put("lensModel", Picture::getLensModel);
        map.put("fNumber", Picture::getFNumber);
        map.put("iso", Picture::getIso);
        map.put("exposureTime", Picture::getExposureTime);
        map.put("focalLength", Picture::getFocalLength);

        map.put("reviewStatus", Picture::getReviewStatus);
        map.put("reviewerId", Picture::getReviewerId);
        map.put("reviewMessage", Picture::getReviewMessage);

        map.put("userId", Picture::getUserId);
        map.put("createTime", Picture::getCreateTime);

        if (Boolean.TRUE.equals(nullSpaceId)) {
            lambdaQueryWrapper.isNull(Picture::getSpaceId);
        }

        sortFieldMap = Collections.unmodifiableMap(map);
        if (StrUtil.isNotEmpty(sortField)) {
            boolean isAsc = "ascend".equalsIgnoreCase(sortOrder);
            SFunction<Picture, ?> sortFunc = sortFieldMap.get(sortField);
            if (sortFunc != null) {
                lambdaQueryWrapper.orderBy(true, isAsc, sortFunc);
            }
        }

        return lambdaQueryWrapper;

    }

    /**
     * 获取图片封装的方法 为原有的图片关联创建用户的信息
     *
     * @param picture 图片
     * @param request 请求
     * @return 封装类PictureVO
     */
    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {

        // 对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        User loginUser = userService.getLoginUser(request);
        // 获取图片id和登录用户id
        Long pictureId = picture.getId();
        Long loginUserId = loginUser.getId();
        // 获取当前用户是否给他点赞
        boolean hasUserLiked = pictureLikeService.hasUserLiked(loginUserId, pictureId);
        // 获取图片点赞数和收藏数
        Long pictureLikeCount = this.getPictureLikeCount(pictureId);
        Long pictureFavoriteCount = this.getPictureFavoriteCount(pictureId);
        pictureVO.setLikeCount(pictureLikeCount);
        pictureVO.setFavoriteCount(pictureFavoriteCount);
        pictureVO.setHasLiked(pictureLikeService.hasUserLiked(loginUserId, pictureId));
        pictureVO.setHasFavorite(pictureFavoriteService.hasUserFavorite(loginUserId, pictureId));
        // 关联查询用户信息id
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);

        }
        return pictureVO;
    }

    /**
     * 分页获取图片封装类
     *
     * @param picturePage 分页对象
     * @param request     请求
     * @return 分页封装对象
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        // 1.关联查询用户信息
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        // 2.填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    /**
     * 图片校验功能
     *
     * @param picture 传入图片
     */
    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时 id不能为空 有参数校验
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "id不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "introduction过长");
        }
    }

    /**
     * 图片审核功能
     *
     * @param pictureReviewRequest 图片审核请求类
     * @param loginUser            登录用户
     */
    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        // 1.参数校验
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        String reviewMessage = pictureReviewRequest.getReviewMessage();
        PictureReviewsStatusEnum pictureReviewsStatusEnum = PictureReviewsStatusEnum.getEnumByValue(reviewStatus);
        ThrowUtils.throwIf(id == null || pictureReviewsStatusEnum == null || PictureReviewsStatusEnum.Unreviewed.equals(pictureReviewsStatusEnum), ErrorCode.PARAMS_ERROR, "请求参数错误");
        // 2.图片是否存在
        Picture picture = this.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "请求参数错误");
        // 3.原来图片状态和现在相同 是否已经审核
        ThrowUtils.throwIf(picture.getReviewStatus().equals(reviewStatus), ErrorCode.PARAMS_ERROR, "请勿重复审核");
        // 4.修改图片审核状态
        Picture newPicture = new Picture();
        BeanUtil.copyProperties(pictureReviewRequest, newPicture);
        newPicture.setReviewerId(loginUser.getId());
        newPicture.setReviewTime(new Date());
        boolean idUpdate = this.updateById(newPicture);
        ThrowUtils.throwIf(!idUpdate, ErrorCode.SYSTEM_ERROR, "审核失败");
    }

    /**
     * 填充审核相关参数 如果是管理员传的图片 直接过审 用户的设置为待审核
     *
     * @param picture   图片
     * @param loginUser 登录用户
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        // 如果是管理员传的图片 直接过审
        if (userService.isAdmin(loginUser)) {
            picture.setReviewerId(loginUser.getId());
            picture.setReviewTime(new Date());
            picture.setReviewStatus(PictureReviewsStatusEnum.Pass.getValue());
            picture.setReviewMessage("管理员上传，自动审核通过");
        } else {
            //  用户的设置为待审核
            picture.setReviewStatus(PictureReviewsStatusEnum.Unreviewed.getValue());
        }
    }

    /**
     * 批量抓去图片并上传
     *
     * @param pictureUploadByBatchRequest 批量抓去请求参数
     * @param loginUser                   登陆用户
     * @return 上传的图片数量
     */
    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        // 校验参数
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer count = pictureUploadByBatchRequest.getCount();
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();

        String category = pictureUploadByBatchRequest.getCategory();
        String tagsStr = JSONUtil.toJsonStr(pictureUploadByBatchRequest.getTags());

        // 名称前缀默认为搜索关键词 如果用户没有指定前缀 就是用搜索词作为前缀
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }

        ThrowUtils.throwIf(count > 30 || count < 1, ErrorCode.PARAMS_ERROR, "请输入1-30之间的数字");
        // 抓去内容
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;

        try {
            SSLUtil.ignoreSsl();
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "SSL 设置失败：" + e.getMessage());
        }

        // 解析内容
        Element div = document.getElementsByClass("dgControl").first();

        ThrowUtils.throwIf(ObjUtil.isNull(div), ErrorCode.OPERATION_ERROR, "获取页面数据失败");
//        Elements imgElementList = div.select("img.mimg");
        Elements imgElementList = div.select("a.iusc");
        // 遍历抓去到的图片 依次上传图片
        int uploadCount = 0;
        for (Element element : imgElementList) {
//            String fileUrl = element.attr("src");
            String m_attr = element.attr("m");
            // 将m属性转换为map对象
            Map<String, String> map = JSONUtil.toBean(m_attr, Map.class);
            String fileUrl = map.get("murl");
            if (StrUtil.isBlank(fileUrl)) {

                log.info("页面 {} 获取页面数据失败", fileUrl);
                continue;
            }
            // 处理图片 放置转移或者和对象存储冲突 删除查询
            int indexOf = fileUrl.indexOf("?");
            if (indexOf > -1) {
                fileUrl = fileUrl.substring(0, indexOf);
            }
            // 调用上传图片接口
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setFileUrl(fileUrl);
            pictureUploadRequest.setCategory(category);
            pictureUploadRequest.setTags(tagsStr);
            pictureUploadRequest.setFileName(namePrefix + (uploadCount + 1));
            // todo 写一个接口 批量上传图片
            try {
                this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                uploadCount++;
            } catch (Exception e) {
                log.error("上传图片失败", e);
                continue;
            }
            // 如果当前上传图片的数量大于指定的 则直接跳出循环
            if (uploadCount >= count) {
                break;
            }
        }

        // 上传图片
        return uploadCount;
    }

    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {
        // 判断该图片是否被多条记录使用 如果是 不能删除
        String oldPictureUrl = oldPicture.getUrl();
        Long count = this.lambdaQuery().eq(Picture::getUrl, oldPictureUrl).count();
        if (count > 1) {
            return;
        }
        // 删除压缩以后的图片
        cosManager.deleteObject(oldPictureUrl);
        // 删除缩略图
        String thumbnailUrl = oldPicture.getThumbnailUrl();
        if (StrUtil.isNotBlank(thumbnailUrl)) {
            cosManager.deleteObject(thumbnailUrl);
        }

        ThrowUtils.throwIf(count > 1, ErrorCode.PARAMS_ERROR, "该图片被其他记录使用，不能删除");
    }

    @Override
    public Long getPictureLikeCount(Long pictureId) {
        return pictureLikeService.countByPictureId(pictureId);
    }

    @Override
    public Long getPictureFavoriteCount(Long pictureId) {
        return pictureFavoriteService.countByPictureId(pictureId);
    }


    /**
     * 校验空间权限
     *
     * @param loginUser 获得用户
     * @param picture   获取图片
     */
    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        // 获取用户身份信息
        Long loginUserId = loginUser.getId();
        // 如果空间id为0，说明是公共空间 此时仅用户自己和管理员可以管理
        Long spaceId = picture.getSpaceId();
        // 获取space的用户id
        if (spaceId == null) {
            // 如果当前用户不是空间创建人并且不是管理员
            ThrowUtils.throwIf(!Objects.equals(loginUserId, picture.getUserId()) && !userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "您没有权限");
        } else {
            // 私有空间 仅空间管理员可以操作
            ThrowUtils.throwIf(!Objects.equals(loginUserId, picture.getUserId()), ErrorCode.NO_AUTH_ERROR, "仅空间管理员可以操作");

        }

    }

    /**
     * 删除 图片
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @Override
    public void deletePicture(DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 修改为封装的的权限校验
        this.checkPictureAuth(loginUser, oldPicture);
        // 开启事务
        transactionTemplate.execute(status -> {
            // 操作数据库
            boolean result = this.removeById(id);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            // 释放额度
            Long spaceId = oldPicture.getSpaceId();
            if (spaceId != null) {
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, spaceId)
                        .setSql("total_size = total_size - " + oldPicture.getPicSize())
                        .setSql("total_count = total_count - 1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "额度更新失败");
            }
            return true;
        });

        this.clearPictureFile(oldPicture);
    }

    @Override
    public void editPicture(PictureEditRequest editRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(editRequest == null || editRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 将实体类和DTO转换
        Picture picture = new Picture();
        BeanUtil.copyProperties(editRequest, picture);
        // 将list转为String
        picture.setTags(JSONUtil.toJsonStr(editRequest.getTags()));
        // 设置编辑时间
        picture.setUpdateTime(new Date());
        // 数据校验图片
        this.validPicture(picture);
        User loginUser = userService.getLoginUser(request);
        // 填入审核参数
        this.fillReviewParams(picture, loginUser);
        // 判断是否存在
        long id = editRequest.getId();
        Picture oldPicture = this.getById(id);
        // 如果老图片为空 代表
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 校验逻辑
        this.checkPictureAuth(loginUser, oldPicture);
        // 操作数据库更新 根据picture有的值去更新数据库中相应位置的标签 此时数据库中是已经上传成功这些图片的
        boolean isEdit = this.updateById(picture);
        ThrowUtils.throwIf(!isEdit, ErrorCode.OPERATION_ERROR);
    }

    /**
     * 根据颜色搜索图片
     *
     * @param spaceId  空间id
     * @param picColor 图片颜色 16
     * @param request  用户获取登录用户
     * @return 按照相似度 返回一个PictureVO列表
     */
    @Override
    public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        // 1. 参数校验
        // 1.1 传入参数不能为空
        ThrowUtils.throwIf(spaceId == null || StrUtil.isBlank(picColor), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.PARAMS_ERROR);
        // 1.2 用户有这个空间相关权限
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        ThrowUtils.throwIf(!loginUser.getId().equals(space.getUserId()), ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
        // 2.查询该空间下所有有主色调的图片
        // 2.1 首先查询有主色调的图片
        List<Picture> pictureList = this.lambdaQuery().eq(Picture::getSpaceId, spaceId).isNotNull(Picture::getPicColor).list();
        // 如果没有图片 直接返回空列表
        if (CollUtil.isEmpty(pictureList)) {
            return Collections.emptyList();
        }
        // 将目标颜色转为 Color 对象
        Color targetColor = Color.decode(picColor);
        // 3.计算相似度
        // 4. 计算相似度并排序
        List<Picture> sortedPictures = pictureList.stream()
                .sorted(Comparator.comparingDouble(picture -> {
                    // 提取图片主色调(16进制)
                    String hexColor = picture.getPicColor();
                    // 没有主色调的图片放到最后
                    if (StrUtil.isBlank(hexColor)) {
                        return Double.MAX_VALUE;
                    }
                    Color pictureColor = Color.decode(hexColor);
                    // 越大越相似
                    return -ColorSimilarUtils.calculateSimilarity(targetColor, pictureColor);
                }))
                // 取前 12 个
                .limit(12)
                .collect(Collectors.toList());
        // 转成VO
        return sortedPictures.stream().map(PictureVO::objToVo).collect(Collectors.toList());
    }

    /**
     * 批量编辑图片
     *
     * @param editRequest 编辑请求
     * @param request     请求
     */
    @Override
    public void editPictureByBatch(PictureEditByBatchRequest editRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<Long> pictureIds = editRequest.getPictureIds();
        Long spaceId = editRequest.getSpaceId();
        String category = editRequest.getCategory();
        List<String> tags = editRequest.getTags();
        String nameRule = editRequest.getNameRule();
        // 1. 参数校验
        // 1.1 传入参数不能为空
        ThrowUtils.throwIf(spaceId == null||loginUser==null||CollUtil.isEmpty(pictureIds), ErrorCode.PARAMS_ERROR);
        // 1.2 用户有这个空间相关权限
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        ThrowUtils.throwIf(!loginUser.getId().equals(space.getUserId()), ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
        // 2.查询指定图片（仅选择需要的字段）
        List<Picture> pictureList = this.lambdaQuery()
                .select(Picture::getId, Picture::getSpaceId)
                .eq(Picture::getSpaceId, spaceId)
                .in(Picture::getId, pictureIds)
                .list();
        if(pictureList.isEmpty()){
            return;
        }
        // 3.更新分类和标签
        pictureList.forEach(picture -> {
            if(StrUtil.isNotBlank(category)){
                picture.setCategory(category);
            }
            if(!CollUtil.isEmpty(tags)){
                picture.setTags(JSONUtil.toJsonStr(tags));
            }
        });
        // 4. 批量重命名
        fillPictureWithNameRule(pictureList, nameRule);
        // 5. 操作数据库
        boolean updated = this.updateBatchById(pictureList);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR,"批量编辑失败");
    }

    private void fillPictureWithNameRule(List<Picture> pictureList, String nameRule) {
        if (CollUtil.isEmpty(pictureList) || StrUtil.isBlank(nameRule)) {
            return;
        }
        long count = 1;
        try {
            for(Picture picture : pictureList){
                String pictureName = nameRule.replaceAll("\\{序号}", String.valueOf(count++));
                picture.setName(pictureName);
            }
        }catch (Exception e){
            log.error("名称解析错误",e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"名称解析错误");
        }
    }


}




