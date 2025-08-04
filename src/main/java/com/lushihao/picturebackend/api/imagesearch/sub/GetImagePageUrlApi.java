package com.lushihao.picturebackend.api.imagesearch.sub;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.lushihao.picturebackend.exception.BusinessException;
import com.lushihao.picturebackend.exception.ErrorCode;
import com.lushihao.picturebackend.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取以图搜图页面地址 step1
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-03   21:59
 */
@Slf4j
public class GetImagePageUrlApi {
    /**
     * 获取图片页面地址
     *
     * @param imageUrl
     * @return
     */
    public static String getImagePageUrl(String imageUrl) {
        // 1. 准备请求参数
        Map<String, Object> formData = new HashMap<>();
        formData.put("image", imageUrl);
        formData.put("tn", "pc");
        formData.put("from", "pc");
        formData.put("image_source", "PC_UPLOAD_URL");
        // 获取当前时间戳
        String acsToken = "1754194773421_1754269768693_MksleCGa5exg6ck1zI8GLjyaiiMiw0RPTNVuFWuGjwelPH6/97xtflzD6pP71fz1Dd+eOE82RV4aT15SBMtjvZf5/SV7eFbI8/fQ0QE52dULXTkbEnhrflnc+g19kOGNhD/r4EeRi2c9udbrDcecQ54HWb8U2R5MmGJ0zUgap7zvYQz7v93b6BnPEJIHjLE1m8iLgbPgf3bpS5U76uo4nZ2xntdMoxOq8/riM8/ufFadke+eItGEteOzu1pdFmWWAr2F3i8AsbcIM2qzey70y9uNYFYOuWO8cyRLuR+87xWXslCYUzP0X6bvJCHV/PDKUltU+fR2Ml38+JpuwuphdIj+Mu6qi638cqX5yfQU1h3MZbANwYk0rg3elUYPz+NzT9kwdH7rf0Ev3LiMduWtBoC44YYXubRlJ1ilqLcLzhpZbkOsbvMQ3Vx7QaHhtIlzA2R0rDu26b6ytagNjWkBAr43eFkffuEnhiDn/YbI8dM=";
        long uptime = System.currentTimeMillis();
        // 请求地址
        String url =
                "https://graph.baidu.com/upload?uptime=" + uptime;

        try {
            // 2. 发送 POST 请求到百度接口
            HttpResponse response = HttpRequest.post(url)
                    .header("acs-token", acsToken)
                    .form(formData)
                    .timeout(10000)
                    .execute();
            // 判断响应状态
            if (HttpStatus.HTTP_OK != response.getStatus()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            // 解析响应
            String responseBody = response.body();
            Map<String, Object> result = JSONUtil.toBean(responseBody, Map.class);

            // 3. 处理响应结果
            if (result == null || !Integer.valueOf(0).equals(result.get("status"))) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            String rawUrl = (String) data.get("url");
            // 对 URL 进行解码
            String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
            // 如果 URL 为空
            if (searchResultUrl == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "未返回有效结果");
            }
            return searchResultUrl;
        } catch (Exception e) {
            log.error("搜索失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }

    public static void main(String[] args) {
        // 测试以图搜图功能
        String imageUrl = "https://www.codefather.cn/logo.png";
//        String imageUrl = "https%3A%2F%2Fwww.codefather.cn%2Flogo.png";
        String result = getImagePageUrl(imageUrl);
        System.out.println("搜索成功，结果 URL：" + result);
    }
}
