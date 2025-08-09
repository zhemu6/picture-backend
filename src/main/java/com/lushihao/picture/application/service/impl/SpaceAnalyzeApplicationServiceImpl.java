package com.lushihao.picture.application.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lushihao.picture.application.service.PictureApplicationService;
import com.lushihao.picture.application.service.SpaceAnalyzeApplicationService;
import com.lushihao.picture.application.service.SpaceApplicationService;
import com.lushihao.picture.application.service.UserApplicationService;
import com.lushihao.picture.domain.picture.entity.Picture;
import com.lushihao.picture.domain.space.entity.Space;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.infrastructure.exception.BusinessException;
import com.lushihao.picture.infrastructure.exception.ErrorCode;
import com.lushihao.picture.infrastructure.exception.ThrowUtils;
import com.lushihao.picture.interfaces.dto.space.analyze.*;
import com.lushihao.picture.interfaces.vo.space.analye.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 空间分析实现类
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-05   8:49
 */
@Service
public class SpaceAnalyzeApplicationServiceImpl implements SpaceAnalyzeApplicationService {


    @Resource
    private UserApplicationService userApplicationService;
    @Resource
    private SpaceApplicationService spaceApplicationService;
    @Resource
    private PictureApplicationService pictureApplicationService;

    @Override
    public SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userApplicationService.getLoginUser(request);
        // 1.参数校验
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR, "请求参数错误");
        SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
        // 判断是查询所有、公共 还是 只查自己的
        if (spaceUsageAnalyzeRequest.isQueryAll() || spaceUsageAnalyzeRequest.isQueryPublic()) {
            // 查询所有的空间使用用量 或者是公共图库 此时需要管理员权限 从picture空间查询用量
            checkSpaceAnalyzeAuth(spaceUsageAnalyzeRequest, loginUser);
            // 构造查询条件
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("pic_size");
            // 补充查询范围
            fillAnalyzeQueryWrapper(spaceUsageAnalyzeRequest, queryWrapper);

            // 从数据库中 查询统计只返回需要的数据 这个是查询图片的占用大小
            List<Object> pictureObjectList = pictureApplicationService.selectPictureSizes(queryWrapper);

            // 统计所有使用的大小
            long usedSize = pictureObjectList.stream().mapToLong(obj -> (Long) obj).sum();
            long usedCount = pictureObjectList.size();
            // 封装返回结果

            spaceUsageAnalyzeResponse.setUsedSize(usedSize);
            // 公共图库没有数量和大小的限制
            spaceUsageAnalyzeResponse.setMaxSize(null);
            spaceUsageAnalyzeResponse.setSizeUsageRatio(null);
            spaceUsageAnalyzeResponse.setUsedCount(usedCount);
            spaceUsageAnalyzeResponse.setMaxCount(null);
            spaceUsageAnalyzeResponse.setCountUsageRatio(null);

        } else {
            // 用户查询自己的空间 直接从space 表查询
            Long spaceId = spaceUsageAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR, "空间id错误");
            Space space = spaceApplicationService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // 权限校验 只有用户 或者是管理员可以查看特定空间的使用情况
            spaceApplicationService.checkSpaceAuth(space, loginUser);
            // 封装返回结果
            spaceUsageAnalyzeResponse.setUsedSize(space.getTotalSize());
            spaceUsageAnalyzeResponse.setMaxSize(space.getMaxSize());
            spaceUsageAnalyzeResponse.setUsedCount(space.getTotalCount());
            spaceUsageAnalyzeResponse.setMaxCount(space.getMaxCount());
            // 后端直接算好百分比，这样前端可以直接展示
            // todo 添加对于这两个百分比的参数校验
            double sizeUsageRatio = NumberUtil.round(space.getTotalSize() * 100.0 / space.getMaxSize(), 2).doubleValue();
            double countUsageRatio = NumberUtil.round(space.getTotalCount() * 100.0 / space.getMaxCount(), 2).doubleValue();
            ThrowUtils.throwIf(sizeUsageRatio > 100 || countUsageRatio > 100 || sizeUsageRatio < 0 || countUsageRatio < 0, ErrorCode.PARAMS_ERROR, "百分比计算错误");
            spaceUsageAnalyzeResponse.setCountUsageRatio(countUsageRatio);
            spaceUsageAnalyzeResponse.setSizeUsageRatio(sizeUsageRatio);
        }
        return spaceUsageAnalyzeResponse;
    }


    /**
     * 获取空间中图片的分类统计情况
     *
     * @param spaceCategoryAnalyzeRequest 图片类别统计请求
     * @param request                     获取用户信息
     * @return SpaceCategoryAnalyzeResponse的list集合
     */
    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, HttpServletRequest request) {
        User loginUser = userApplicationService.getLoginUser(request);
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 权限校验
        checkSpaceAnalyzeAuth(spaceCategoryAnalyzeRequest, loginUser);
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceCategoryAnalyzeRequest, queryWrapper);
        queryWrapper.select("category AS category",
                        "COUNT(*) AS count",
                        "SUM(pic_size) AS totalSize")
                .groupBy("category");
        List<Map<String, Object>> queryResult = pictureApplicationService.getCategoryStatistics(queryWrapper);


        return queryResult.stream().map(result -> {
            String category = result.get("category") != null ? result.get("category").toString() : "未分类";
            Long count = ((Number) result.get("count")).longValue();
            Long totalSize = ((Number) result.get("totalSize")).longValue();
            return new SpaceCategoryAnalyzeResponse(category, count, totalSize);
        }).collect(Collectors.toList());
    }

    /**
     * 获取空间中图片的分标签统计情况
     *
     * @param spaceTagAnalyzeRequest 图片分标签统计情况
     * @param request                获取用户信息
     * @return SpaceTagAnalyzeResponse集合 包含每个标签的名称数量
     */
    @Override
    public List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, HttpServletRequest request) {
        User loginUser = userApplicationService.getLoginUser(request);
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 权限校验
        checkSpaceAnalyzeAuth(spaceTagAnalyzeRequest, loginUser);
        // 构造权限
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceTagAnalyzeRequest, queryWrapper);
        // 因为tags里面存放的是 ["搞笑","高清"] 这样的 我们先从中去除
        queryWrapper.select("tags");
        List<String> tagsJsonList = pictureApplicationService.selectTageJsonList(queryWrapper);
        // 将这个list集合统计合并 定义成一个map集合 key是标签 value是数量
        Map<String, Long> tagCountMap = tagsJsonList.stream()
                .flatMap(tagsJson -> JSONUtil.toList(tagsJson, String.class).stream())
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));
        // 将这个tagCountMap 转换成response对象
        return tagCountMap.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .map(entry -> new SpaceTagAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

    }

    /**
     * 获取用户在时间维度上上传图片的统计情况
     *
     * @param spaceUserAnalyzeRequest 用户行为统计
     * @param request                 获取用户信息
     * @return SpaceUserAnalyzeResponse集合 包含每个时间点的上传数量
     */
    @Override
    public List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, HttpServletRequest request) {
        User loginUser = userApplicationService.getLoginUser(request);
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 权限校验
        checkSpaceAnalyzeAuth(spaceUserAnalyzeRequest, loginUser);
        // 构造权限
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        // 获取用户id
        String userId = spaceUserAnalyzeRequest.getUserId();
        queryWrapper.eq(ObjUtil.isNotNull(userId), "user_id", userId);
        fillAnalyzeQueryWrapper(spaceUserAnalyzeRequest, queryWrapper);
        // 获取时间维度
        String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
        switch (timeDimension) {
            case "day":
                queryWrapper.select("DATE_FORMAT(create_time, '%Y-%m-%d') AS period", "COUNT(*) AS count");
                break;
            case "week":
                queryWrapper.select("YEARWEEK(create_time) AS period", "COUNT(*) AS count");
                break;
            case "month":
                queryWrapper.select("DATE_FORMAT(create_time, '%Y-%m') AS period", "COUNT(*) AS count");
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的时间维度");
        }
        // 分组并且按照时间升序排序
        queryWrapper.groupBy("period").orderByAsc("period");

        // 查询
        List<Map<String, Object>> queryResult = pictureApplicationService.selectTime(queryWrapper);

        return queryResult.stream()
                .map(result -> {
                    String period = result.get("period").toString();
                    Long count = ((Number) result.get("count")).longValue();
                    return new SpaceUserAnalyzeResponse(period, count);
                }).collect(Collectors.toList());

    }

    /**
     * 取出前10个空间 （用量）的统计情况
     *
     * @param spaceRankAnalyzeRequest 空间排名请求
     * @param request                 获取用户信息
     * @return Space集合  返回前topk个占用space
     */
    @Override
    public List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, HttpServletRequest request) {
        User loginUser = userApplicationService.getLoginUser(request);
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 权限校验 只有管理员可以查询
        ThrowUtils.throwIf(!loginUser.isAdmin(), ErrorCode.NO_AUTH_ERROR);
        // 构造权限
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "space_name", "user_id", "total_size").orderByDesc("total_size").last("LIMIT " + spaceRankAnalyzeRequest.getTopK());
        // 查询结果
        return spaceApplicationService.list(queryWrapper);
    }

    /**
     * 获取图片不同大小范围上的数量分布
     *
     * @param spaceSizeAnalyzeRequest 空间区间请求
     * @param request                 获取用户信息
     * @return SpaceSizeAnalyzeResponse集合  返回图片不同大小范围上的数量分布
     */
    @Override
    public List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, HttpServletRequest request) {
        User loginUser = userApplicationService.getLoginUser(request);
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 权限校验
        checkSpaceAnalyzeAuth(spaceSizeAnalyzeRequest, loginUser);
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceSizeAnalyzeRequest, queryWrapper);
        queryWrapper.select("pic_size");

        List<Long> picSizes = pictureApplicationService.getPictureSizes(queryWrapper);


        // 定义分段范围，注意使用有序 Map
        Map<String, Long> sizeRanges = new LinkedHashMap<>();
        sizeRanges.put("<5MB", picSizes.stream().filter(size -> size < 5 * 1024 * 1024).count());
        sizeRanges.put("5MB-10MB", picSizes.stream().filter(size -> size >= 5 * 1024 * 1024 && size < 10 * 1024 * 1024).count());
        sizeRanges.put("10MB-15MB", picSizes.stream().filter(size -> size >= 10 * 1024 * 1024 && size < 15 * 1024 * 1024).count());
        sizeRanges.put(">15MB", picSizes.stream().filter(size -> size >= 15 * 1024 * 1024).count());

        // 转换为响应对象
        return sizeRanges.entrySet().stream()
                .map(entry -> new SpaceSizeAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 空间分析权限校验
     *
     * @param spaceAnalyzeRequest 空间分析请求类
     * @param loginUser           当前登录用户
     */
    private void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        // 如果是要查询所有的公共图库或者是查询所有空间 用户需要是管理员
        if (spaceAnalyzeRequest.isQueryAll() || spaceAnalyzeRequest.isQueryPublic()) {
            ThrowUtils.throwIf(!loginUser.isAdmin(), ErrorCode.NO_AUTH_ERROR, "当前用户没有权限查询所有空间");
        } else {
            // 用户查询的是自己的
            Long spaceId = spaceAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId != null && spaceId <= 0, ErrorCode.PARAMS_ERROR, "空间id错误");
            Space space = spaceApplicationService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            spaceApplicationService.checkSpaceAuth(space, loginUser);
        }
    }

    /**
     * 根据SpaceAnalyzeRequest中的字段来补充LambdaQueryWrapper中相应的字段
     *
     * @param spaceAnalyzeRequest 查询请求
     * @param queryWrapper        查询条件
     */
    private static void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {
        // 如果是查询所有 也就是所有的空间 直接返回
        if (spaceAnalyzeRequest.isQueryAll()) {
            return;
        }
        // 如果是查询公共图库
        if (spaceAnalyzeRequest.isQueryPublic()) {
            queryWrapper.isNull("space_id");
            return;
        }
        // 如果用户只查自己的
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (spaceId != null) {
            queryWrapper.eq("space_id", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
    }


}
