package com.lushihao.picture.shared.websocket.disruptor;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * Disruptor 的配置
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-08   10:31
 */
@Configuration
public class PictureEditEventDisruptorConfig {
    @Resource
    private PictureEditEventWorkHandler pictureEditEventWorkHandler;

    @Bean("pictureEditEventDisruptor")
    public Disruptor<PictureEditEvent> messageModelRingBuffer() {
        // 定义ring buffer的大小
        int bufferSize = 1024 * 256;
        // 创建disruptor
        Disruptor<PictureEditEvent> pictureEditEventDisruptor = new Disruptor<>(
                PictureEditEvent::new,
                bufferSize,
                ThreadFactoryBuilder.create()
                        .setNamePrefix("pictureEditEventDisruptor").build()
        );
        // 设置消费者
        pictureEditEventDisruptor.handleEventsWithWorkerPool(pictureEditEventWorkHandler);
        // 启动disruptor
        pictureEditEventDisruptor.start();

        return pictureEditEventDisruptor;

    }

}
