package com.lushihao.picturebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.lushihao.picturebackend.common.ResultUtils;
import com.lushihao.picturebackend.config.CosClientConfig;
import com.lushihao.picturebackend.exception.BusinessException;
import com.lushihao.picturebackend.exception.ErrorCode;
import com.lushihao.picturebackend.exception.ThrowUtils;
import com.lushihao.picturebackend.model.dto.file.UploadPictureResult;
import com.lushihao.picturebackend.util.ExifUtil;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.UploadPartResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.OriginalInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 通用的文件上传服务
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-30   10:08
 */
@Slf4j
@Service
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     *
     * @param multipartFile    文件
     * @param uploadPathPrefix 上传路径前缀
     * @return UploadPictureResult
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        // 校验图片
        validPicture(multipartFile);
        // 图片上传地址
        String uuid = RandomUtil.randomString(16);
        // 获取原始文件名
        String originFileName = multipartFile.getOriginalFilename();
        // 上传的文件名 时间戳_uuit.后缀
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()),uuid,FileUtil.getSuffix(originFileName));
        // 上传的路径
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        File file = null;
        try {
            file = File.createTempFile(uploadPath,null);
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            log.info("putObjectResult = {}", putObjectResult);
            log.info("ciUploadResult = {}", putObjectResult.getCiUploadResult());
            log.info("originalInfo = {}", putObjectResult.getCiUploadResult().getOriginalInfo());
            log.info("imageInfo = {}", putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo());
            // 获得原始信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 创建一个图片上传结果类
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
            uploadPictureResult.setPicName(FileUtil.mainName(originFileName));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            // 读取 EXIF 拍摄参数信息并填充
            ExifUtil.fillExifInfo(file, uploadPictureResult);
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("file upload error, filepath = " + uploadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }finally {
            //删除临时文件
            deleteTempFile(file);
        }

    }

    /**
     * 删除临时文件
     * @param file
     */
    public static void deleteTempFile(File file) {
        if(file==null){
            return;
        }
        // 删除临时文件
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }

    /**
     * 校验图片
     * @param multipartFile multipartFile为毛啊吗
     */
    private void validPicture(MultipartFile multipartFile) {
        // 图片不能为空
        ThrowUtils.throwIf(multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR,"文件不能为空");
        // 校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(fileSize > 20 * ONE_MB, ErrorCode.PARAMS_ERROR,"文件大小不能超过20MB");
        // 校验文件格式/后缀 都同意转换为小写
        String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename()).toLowerCase();
        // 允许上传的图片后缀
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "jpg", "png", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(suffix), ErrorCode.PARAMS_ERROR,"文件格式不正确");
    }

}
