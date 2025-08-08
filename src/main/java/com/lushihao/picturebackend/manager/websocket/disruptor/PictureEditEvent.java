package com.lushihao.picturebackend.manager.websocket.disruptor;

import com.lushihao.picturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.lushihao.picturebackend.model.entity.User;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

/**
 * 图片编辑事件
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-08   10:23
 */
@Data
public class PictureEditEvent {

    /**
     * 消息
     */
    private PictureEditRequestMessage pictureEditRequestMessage;

    /**
     * 当前用户的session
     */
    private WebSocketSession session;

    /**
     * 当前用户
     */
    private User user;

    /**
     * 图片id
     */
    private Long pictureId;
}
