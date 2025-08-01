package com.lushihao.picturebackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.lushihao.picturebackend.config.CosClientConfig;
import com.lushihao.picturebackend.exception.BusinessException;
import com.lushihao.picturebackend.exception.ErrorCode;
import com.lushihao.picturebackend.exception.ThrowUtils;
import com.lushihao.picturebackend.manager.CosManager;
import com.lushihao.picturebackend.model.dto.file.UploadAvatarResult;
import com.lushihao.picturebackend.model.dto.file.UploadPictureResult;
import com.lushihao.picturebackend.util.ExifUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.UploadResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 图片上传模板方法
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-30   10:08
 */
@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     *
     * @param inputSource    文件
     * @param uploadPathPrefix 上传路径前缀
     * @return UploadPictureResult
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 1.校验图片
        validPicture(inputSource);
        // 2.图片上传地址
        String uuid = RandomUtil.randomString(16);
        // 获取原始文件名
        String originFileName = getOriginalFilename(inputSource);
        // 上传的文件名 时间戳_uuit.后缀
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originFileName));
        // 上传的路径
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        File file = null;
        try {
            // 3.创建临时文件并处理文件
            file = File.createTempFile(uploadPath, null);
            // 处理文件来源
            processFile(inputSource,file);
            // 4.上传文件并获取对象存储
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            // 5.获得原始信息并封装返回结果
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 6. 获取到图片处理结果
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if(CollUtil.isNotEmpty(objectList)){
                CIObject compressCiObject = objectList.get(0);
                // 缩略图默认为压缩的
                CIObject thumbnailObject = compressCiObject;
                // 如果他有缩略图 就改一下
                if(objectList.size()>1){
                    thumbnailObject = objectList.get(1);
                }
                return buildResult(originFileName,file,thumbnailObject,compressCiObject);
            }
            // 封装原图返回结果
            return buildResult(imageInfo,originFileName,file,uploadPath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + uploadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            //6.删除临时文件
            deleteTempFile(file);
        }

    }





    /**
     * 获取图片返回的记过
     * @param originFileName
     * @param compressCiObject
     * @return
     */
    private UploadPictureResult buildResult(String originFileName,File file,CIObject thumbnailObject, CIObject compressCiObject) {
        // 创建一个图片上传结果类
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = compressCiObject.getWidth();
        int picHeight = compressCiObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setPicName(FileUtil.mainName(originFileName));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        // 设置缩略图地址
        uploadPictureResult.setThumbnailUrl( cosClientConfig.getHost() + "/" + thumbnailObject.getKey());
        uploadPictureResult.setPicFormat(compressCiObject.getFormat());
        uploadPictureResult.setPicSize(Long.valueOf(compressCiObject.getSize()));
        // 设置压缩后的原图地址
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressCiObject.getKey());
        // 读取 EXIF 拍摄参数信息并填充
        ExifUtil.fillExifInfo(file, uploadPictureResult);
        return uploadPictureResult;
    }


    /**
     * 处理图片
     * @param inputSource
     */
    protected abstract void processFile(Object inputSource,File file) throws IOException;

    /**
     * 获取输入源的原始文件名
     * @param inputSource 输入源
     * @return
     */
    protected abstract String getOriginalFilename(Object inputSource);

    /**
     * 接受输入源验证是否合法
     * @param inputSource 输入源
     */
    protected abstract void validPicture(Object inputSource);


    /**
     * 删除临时文件
     *
     * @param file
     */
    public static void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        // 删除临时文件
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }

    /**
     * 构建返回结果
     * @param imageInfo 对象存储返回图片信息
     * @param originFileName 原始文件名
     * @param file 文件
     * @param uploadPath
     * @return
     */
    private UploadPictureResult buildResult(ImageInfo imageInfo, String originFileName, File file, String uploadPath) {
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
    }






}
