package com.lushihao.picturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.lushihao.picturebackend.exception.BusinessException;
import com.lushihao.picturebackend.exception.ErrorCode;
import com.lushihao.picturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-31   20:53
 */
@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    /**
     * 图片后缀类型，防止并发问题
     */
    private final AtomicReference<String> typeRef = new AtomicReference<>();


    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        String fileUrl = (String) inputSource;
        // 下载文件到临时目录
        HttpUtil.downloadFile(fileUrl, file);
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        String fileUrl = (String) inputSource;

        // 去掉 URL 参数，只保留 ? 之前
        int queryIndex = fileUrl.indexOf("?");
        if (queryIndex != -1) {
            fileUrl = fileUrl.substring(0, queryIndex);
        }

        // 获取后缀（例如 jpg）
        String suffix = FileUtil.getSuffix(fileUrl);
        final List<String> ALLOW_SUFFIX = Arrays.asList("png", "jpeg", "jpg", "webp");

        // 若不是允许的后缀，追加默认后缀（例如 .jpg）
        if (!ALLOW_SUFFIX.contains(suffix)) {
            fileUrl = fileUrl + typeRef.get();
        }
        // 取干净的文件名
        return FileUtil.getName(fileUrl);
    }


    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        // 校验非空
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件url不能为空");
        // 格式正确
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件url格式不正确");
        }
        // 协议校验
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"), ErrorCode.PARAMS_ERROR, "仅支持http或https协议的文件地址");
        // 发送head请求
        HttpResponse httpResponse = null;
        try {
            httpResponse = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            // 文件类型校验
            String contentType = httpResponse.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                // 允许的图片类型
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                        ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            // 文件大小校验
            String contentLengthStr = httpResponse.header("Content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)) {
                try {

                    final long ONE_MB = 1024 * 1024L;
                    long contentLength = Long.parseLong(contentLengthStr);
                    // 允许的图片类型
                    ThrowUtils.throwIf(contentLength > 20 * ONE_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过20MB");
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
                }
            }
        } finally {
            // 资源释放
            if (httpResponse != null)
                httpResponse.close();
        }
    }
}
