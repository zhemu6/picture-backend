package com.lushihao.picture.shared.websocket;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.lushihao.picture.shared.auth.SpaceUserAuthManager;
import com.lushihao.picture.shared.auth.model.SpaceUserPermissionConstant;
import com.lushihao.picture.domain.picture.entity.Picture;
import com.lushihao.picture.domain.space.entity.Space;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.domain.space.valueobject.SpaceTypeEnum;
import com.lushihao.picture.application.service.PictureApplicationService;
import com.lushihao.picture.application.service.SpaceApplicationService;
import com.lushihao.picture.application.service.SpaceUserApplicationService;
import com.lushihao.picture.application.service.UserApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-07   18:08
 */
@Slf4j
@Component
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    @Resource
    private UserApplicationService userApplicationService;
    @Resource
    private PictureApplicationService pictureApplicationService;
    @Resource
    private SpaceApplicationService spaceApplicationService;
    @Resource
    private SpaceUserApplicationService spaceUserApplicationService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 握手前执行的操作
     *
     * @param request
     * @param response
     * @param wsHandler
     * @param attributes 给websocketsession会话设置属性
     * @return
     * @throws Exception
     */

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 权限校验
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            String pictureId = servletRequest.getParameter("pictureId");
            if (StrUtil.isBlank(pictureId)) {
                log.error("图片id为空，拒绝握手");
                return false;
            }
            User loginUser = userApplicationService.getLoginUser(servletRequest);
            if (ObjectUtil.isEmpty(loginUser)) {
                log.error("用户未登录，拒绝握手");
                return false;
            }
            // 校验用户是否有编辑当前图片的权限
            Picture picture = pictureApplicationService.getById(Long.parseLong(pictureId));
            if (ObjectUtil.isEmpty(picture)) {
                log.error("图片不存在，拒绝握手");
                return false;
            }
            Long spaceId = picture.getSpaceId();
            Space space = null;
            // 只有在团队空间 才可以实现实时共享编辑 并且有编辑权限 才能建立连接
            if (spaceId != null) {
                // 私有空间/团队空间
                space = spaceApplicationService.getById(spaceId);
                if (ObjectUtil.isEmpty(space)) {
                    log.error("图片所在空间不存在，拒绝握手");
                    return false;
                }

                if (space.getSpaceType() != SpaceTypeEnum.TEAM.getValue()) {
                    log.error("图片所在空间不是团队空间，拒绝握手");
                    return false;
                }
            }
            List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
            if (!permissionList.contains(SpaceUserPermissionConstant.PICTURE_EDIT)) {
                log.error("用户没有编辑权限，拒绝握手");
                return false;
            }
            // 设置用户登录等属性到websocket
            attributes.put("user", loginUser);
            attributes.put("userId", loginUser.getId());
            // 记得转化为Long类型
            attributes.put("pictureId", Long.valueOf(pictureId));

        }

        return true;
    }

    /**
     * 握手后执行的操作
     *
     * @param request
     * @param response
     * @param wsHandler
     * @param exception
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
