package com.lushihao.picturebackend.manager;

import cn.hutool.core.io.FileUtil;
import com.lushihao.picturebackend.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;

/**
 * Ma؜nager 也是人为约定的一种写法，表示通用的、可复用的能力，可供其他代码（比如Service）调用。
 * 用于提供通用的对象存储操作
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-29   11:06
 */
@Component
public class CosManager {
    @Resource
    private CosClientConfig cosClientConfig;
    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }


    /**
     * 下载对象
     * @param key  唯一键
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }


    /**
     * 图片上传功能 附带图片信息
     * @param key  唯一键
     * @param file 文件
     * @return PutObjectResult
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        // 对图片进行处理（获取基本信息也被视作为一种处理）
        PicOperations picOperations = new PicOperations();
        // 1 表示返回原图信息
        picOperations.setIsPicInfo(1);
        List<PicOperations.Rule> rules = new ArrayList<>();
        // 还对图片进行压缩转换成webp格式
        String webpKey = FileUtil.mainName(key)+".webp";
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setFileId(webpKey);
        compressRule.setRule("imageMogr2/format/webp");
        compressRule.setBucket(cosClientConfig.getBucket());
        rules.add(compressRule);
        // 仅对对于大于20kb的图片进行处理
        if(file.length() > 20 * 1024) {
            // 缩略图处理
            PicOperations.Rule thumbnailRule  = new PicOperations.Rule();
            String thumbnailKey = FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key);
            thumbnailRule.setFileId(thumbnailKey);
            // 缩放规则 /thumbnail/<Width>x<Height>>（如果大于原图宽高，则不处理）1
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 512, 512));
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            rules.add(thumbnailRule);
        }
        // 构造处理参数
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 删除操作
     * @param key  唯一键
     */
    public void deleteObject(String key) {
        cosClient.deleteObject(cosClientConfig.getBucket(),key);
    }

    /**
     * 获取对象的完整访问 URL
     * @param key 对象在 COS 中的 key（路径，如 "/avatar/xxx.png" 或 "avatar/xxx.png"）
     * @return 完整的访问 URL
     */
    public String getAccessUrl(String key) {
        // 清除前缀的 "/"，避免出现两个 //
        if (key.startsWith("/")) {
            key = key.substring(1);
        }
        return cosClientConfig.getBaseUrl() + "/" + key;
    }



}
