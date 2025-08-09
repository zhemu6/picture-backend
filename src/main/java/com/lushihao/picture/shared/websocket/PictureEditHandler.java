package com.lushihao.picture.shared.websocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.lushihao.picture.shared.websocket.disruptor.PictureEditEventProducer;
import com.lushihao.picture.shared.websocket.model.PictureEditActionEnum;
import com.lushihao.picture.shared.websocket.model.PictureEditMessageTypeEnum;
import com.lushihao.picture.shared.websocket.model.PictureEditRequestMessage;
import com.lushihao.picture.shared.websocket.model.PictureEditResponseMessage;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.application.service.UserApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 处理器
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-07   20:21
 */
@Slf4j
@Component
public class PictureEditHandler extends TextWebSocketHandler {

    // 每张图片的编辑状态，key: pictureId, value: 当前正在编辑的用户 ID
    private final Map<Long, Long> pictureEditingUsers = new ConcurrentHashMap<>();

    // 保存所有连接的会话，key: pictureId, value: 用户会话集合
    private final Map<Long, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();

    @Resource
    private UserApplicationService userApplicationService;
    @Resource
    @Lazy
    private PictureEditEventProducer pictureEditEventProducer;

    /**
     * 建立连接之后调用
     *
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 保存会话到集合中
        // 获取session中的用户
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        // 如果是第一次加入，则要初始化一个 pictureSessions是一个map集合
        pictureSessions.putIfAbsent(pictureId, ConcurrentHashMap.newKeySet());
        // 拿到刚才新建的map集合 将session插入到key为pictureId中
        pictureSessions.get(pictureId).add(session);
        // 构造相应，发送加入编辑的消息通知
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        // 设置编辑相应的相关属性
        // 通知类型
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        // 设置消息
        String message = String.format("用户 %s 加入编辑协作中", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userApplicationService.getUserVO(user));

        // 广播给该图片中所有的用户(包括自己)
        broadcastToPicture(pictureId, pictureEditResponseMessage);

    }

    /**
     * 收到发送的消息 根据信息类别处理信息
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        // 获取消息内容、将JSON转换为PictureEditMessage
        PictureEditRequestMessage pictureEditRequestMessage = JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);
        String type = pictureEditRequestMessage.getType();
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        // 生产消息到Disruptor环形队列中
        pictureEditEventProducer.publishEvent(pictureEditRequestMessage, session, user, pictureId);

        // 获取相应对的消息类型
        PictureEditMessageTypeEnum enumByValue = PictureEditMessageTypeEnum.getEnumByValue(type);

        // 根据消息类型处理
        switch (enumByValue) {
            case ENTER_EDIT:
                handleEnterEditMessage(pictureEditRequestMessage, session, user, pictureId);
                break;
            case EXIT_EDIT:
//                log.info("执行退出操作");
                handleExitEditMessage(pictureEditRequestMessage, session, user, pictureId);
                break;
            case EDIT_ACTION:
                handleEditActionMessage(pictureEditRequestMessage, session, user, pictureId);
                break;
            default:
                // 其他消息类型，参数有误，返回错误提示
                PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
                pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ERROR.getValue());
                pictureEditResponseMessage.setMessage("消息类型有误");
                pictureEditResponseMessage.setUser(userApplicationService.getUserVO(user));
                session.sendMessage(new TextMessage(JSONUtil.toJsonStr(pictureEditResponseMessage)));
                throw new IllegalStateException("Unexpected value: " + enumByValue);
        }
    }

    /**
     * 进入编辑状态
     *
     * @param pictureEditRequestMessage
     * @param session
     * @param user
     * @param pictureId
     */
    public void handleEnterEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws IOException {
        // 没用用户正在编辑该图片，才能进入编辑
        if (!pictureEditingUsers.containsKey(pictureId)) {
            // 设置当前图片的编辑用户为该用户
            pictureEditingUsers.put(pictureId, user.getId());
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            // 设置编辑相应的相关属性
            // 通知类型
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
            // 设置消息
            String message = String.format("用户 %s 开始编辑图片", user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userApplicationService.getUserVO(user));
            // 广播给该图片中所有的用户(包括自己)
            broadcastToPicture(pictureId, pictureEditResponseMessage);

        }
    }

    /**
     * 处理编辑操作
     *
     * @param pictureEditRequestMessage
     * @param session
     * @param user
     * @param pictureId
     */
    public void handleEditActionMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws IOException {
        // 首先看当前用户是不是编辑者
        Long editingUserId = pictureEditingUsers.get(pictureId);
        String editAction = pictureEditRequestMessage.getEditAction();
        PictureEditActionEnum actionEnum = PictureEditActionEnum.getEnumByValue(editAction);
        if (actionEnum == null) {
            //用户传入了一个不支持的动作
            log.error("无效的编辑动作");
            return;
        }
        // 确定当前登录用户是不是编辑者
        if (editingUserId != null && editingUserId.equals(user.getId())) {
            // 构造响应 发送具体操作的通知
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            // 设置编辑相应的相关属性
            // 通知类型
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            // 设置消息
            String message = String.format("用户 %s 对图片执行了 %s 操作", user.getUserName(), actionEnum.getText());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setEditAction(editAction);
            pictureEditResponseMessage.setUser(userApplicationService.getUserVO(user));
            // 广播给该图片中所有的用户(排除自己)
            broadcastToPicture(pictureId, pictureEditResponseMessage, session);
        }

    }

    /**
     * 退出编辑状态
     *
     * @param pictureEditRequestMessage
     * @param session
     * @param user
     * @param pictureId
     */
    public void handleExitEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws IOException {
        // 首先看当前用户是不是编辑者
        Long editingUserId = pictureEditingUsers.get(pictureId);
        if (editingUserId != null && editingUserId.equals(user.getId())) {
            // 移除当前图片的编辑者
            pictureEditingUsers.remove(pictureId);
            // 构造类型
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
            // 设置消息
            String message = String.format("用户 %s 退出编辑图片", user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userApplicationService.getUserVO(user));
//            log.info("执行了退出编辑动作"+pictureEditResponseMessage.toString());
            // 广播给该图片中所有的用户(排除自己)这里也要通知自己退出 要不然前端不知道是否已经退出
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }


    }


    /**
     * 在连接关闭之后执行的操作
     *
     * @param session
     * @param closeStatus
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        super.afterConnectionClosed(session, closeStatus);
        // 移除当前用户的编辑状态
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        handleExitEditMessage(null, session, user, pictureId);
        // 删除会话
        Set<WebSocketSession> socketSessions = pictureSessions.get(pictureId);
        if (socketSessions != null) {
            // 首先如果当前pictureSession中不为空 先将对话从socketSessions中移除
            socketSessions.remove(session);
            // 如果移除之后socketSessions为空 则将pictureId从pictureSessions中移除
            if (socketSessions.isEmpty()) {
                pictureSessions.remove(pictureId);
            }
        }
        // 通知其他用户 该用户已经离开了编辑协作中
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
        // 设置消息
        String message = String.format("用户 %s 退出了编辑协作", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userApplicationService.getUserVO(user));
        // 广播给该图片中所有的用户(排除自己)
        broadcastToPicture(pictureId, pictureEditResponseMessage, session);
    }

    /**
     * 广播给该图片中的所有用户
     *
     * @param pictureId                  图片id
     * @param pictureEditResponseMessage 图片编辑响应消息 后端传给前端的消息
     * @param excludeSession             被排除掉的session 比如用户自己发送的请求 自己不可能在执行一次
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage, WebSocketSession excludeSession) throws IOException {
        // 当前这个图片的所有会话集合
        Set<WebSocketSession> socketSessions = pictureSessions.get(pictureId);
        if (CollUtil.isNotEmpty(socketSessions)) {
            // 创建 ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            // 配置序列化：将 Long 类型转为 String，解决丢失精度问题
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            // 支持 long 基本类型
            module.addSerializer(Long.TYPE, ToStringSerializer.instance);
            objectMapper.registerModule(module);
            // 序列化为Json字符串
            String message = objectMapper.writeValueAsString(pictureEditResponseMessage);
            TextMessage textMessage = new TextMessage(message);
            for (WebSocketSession socketSession : socketSessions) {
                // 排除掉的 session 不发送
                if (excludeSession != null && excludeSession.equals(socketSession)) {
                    continue;
                }
                if (socketSession.isOpen()) {
                    socketSession.sendMessage(textMessage);
                }
            }
        }
    }

    /**
     * 方法重载 广播给该图片中的所有用户
     *
     * @param pictureId                  图片id
     * @param pictureEditResponseMessage 图片编辑响应消息 后端传给前端的消息
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage) throws IOException {
        // 直接调用刚才的方法，只不过传入的排除的session为null
        broadcastToPicture(pictureId, pictureEditResponseMessage, null);
    }


}
