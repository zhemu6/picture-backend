package com.lushihao.picturebackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.lushihao.picturebackend.exception.BusinessException;
import com.lushihao.picturebackend.exception.ErrorCode;
import com.lushihao.picturebackend.exception.ThrowUtils;
import com.lushihao.picturebackend.manager.CosManager;
import com.lushihao.picturebackend.model.dto.file.UploadAvatarResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-31   20:50
 */
@Slf4j
@Service
public class FilePictureUpload extends PictureUploadTemplate {
    @Resource
    private CosManager cosManager;

    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;

        return multipartFile.getOriginalFilename();
    }

    @Override
    protected void validPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        // 图片不能为空
        ThrowUtils.throwIf(multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(fileSize > 20 * ONE_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过20MB");
        // 校验文件格式/后缀 都同意转换为小写
        String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename()).toLowerCase();
        // 允许上传的图片后缀
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "jpg", "png", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(suffix), ErrorCode.PARAMS_ERROR, "文件格式不正确");

    }

    /**
     * 上传头像，仅返回图片 URL
     *
     * @param uploadFile 文件对象（头像图片）
     * @param uploadPathPrefix 上传路径前缀（如 "avatar"）
     * @return 上传后头像图片访问 URL
     */
    public String uploadAvatar(MultipartFile uploadFile, String uploadPathPrefix) {
        // 1. 校验图片
        validPicture(uploadFile);

        // 2. 构建上传路径
        String uuid = RandomUtil.randomString(16);
        String originFileName = getOriginalFilename(uploadFile);
        String suffix = FileUtil.getSuffix(originFileName);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, suffix);
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);

        File file = null;
        try {
            // 3. 创建临时文件并处理
            file = File.createTempFile("avatar_", "." + suffix);
            processFile(uploadFile, file);

            // 4. 上传文件至对象存储
            cosManager.putPictureObject(uploadPath, file);
            // 5. 返回上传后的图片完整访问 URL
            return cosManager.getAccessUrl(uploadPath);
        } catch (Exception e) {
            log.error("头像上传失败, filepath = {}", uploadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "头像上传失败");
        } finally {
            // 6. 删除临时文件
            deleteTempFile(file);
        }
    }

}


