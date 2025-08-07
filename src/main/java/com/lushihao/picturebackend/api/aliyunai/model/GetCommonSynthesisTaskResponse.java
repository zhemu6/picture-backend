package com.lushihao.picturebackend.api.aliyunai.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 查询任务响应类
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-04   15:43
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetCommonSynthesisTaskResponse {

    /**
     * 请求唯一标识
     */
    @JsonProperty("request_id")
    private String requestId;

    /**
     * 输出信息
     */
    private Output output;

    /**
     * 表示任务的输出信息
     */
    @Data
    public static class Output {

        /**
         * 任务 ID
         */
        private String taskId;

        /**
         * 任务状态
         */
        private String taskStatus;

        /**
         * 提交时间
         */
        private String submitTime;

        /**
         * 调度时间
         */
        private String scheduledTime;

        /**
         * 结束时间
         */
        private String endTime;

        /**
         * 任务结果列表（包含图像 URL）
         */
        private List<Result> results;

        /**
         * 任务指标信息
         */
        private TaskMetrics taskMetrics;

        // 你可以添加一个辅助方法，用来直接获取首张图片 URL
        public String getOutputImageUrl() {
            if (results != null && !results.isEmpty()) {
                return results.get(0).getUrl();
            }
            return null;
        }

        @Data
        public static class Result {
            private String url;
        }

        /**
         * 错误码（如果有）
         */
        private String code;

        /**
         * 错误消息（如果有）
         */
        private String message;
    }

    /**
     * 表示任务的统计信息
     */
    @Data
    public static class TaskMetrics {
        private Integer total;

        private Integer succeeded;

        private Integer failed;
    }
}
