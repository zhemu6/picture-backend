package com.lushihao.picture.shared.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.lushihao.picture.infrastructure.exception.BusinessException;
import com.lushihao.picture.infrastructure.exception.ErrorCode;
import com.lushihao.picture.shared.auth.model.SpaceUserPermissionConstant;
import com.lushihao.picture.domain.picture.entity.Picture;
import com.lushihao.picture.domain.space.entity.Space;
import com.lushihao.picture.domain.space.entity.SpaceUser;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.domain.space.valueobject.SpaceRoleEnum;
import com.lushihao.picture.domain.space.valueobject.SpaceTypeEnum;
import com.lushihao.picture.application.service.PictureApplicationService;
import com.lushihao.picture.application.service.SpaceApplicationService;
import com.lushihao.picture.application.service.SpaceUserApplicationService;
import com.lushihao.picture.application.service.UserApplicationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import static com.lushihao.picture.domain.user.constant.UserConstant.USER_LOGIN_STATE;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 自定义权限加载接口实现类
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-07   9:12
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    /**
     * 获取上下文的请求路径
     */
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;
    @Resource
    private SpaceUserApplicationService spaceUserApplicationService;
    @Resource
    private PictureApplicationService pictureApplicationService;
    @Resource
    private SpaceApplicationService spaceApplicationService;
    @Resource
    private UserApplicationService userApplicationService;
    /**
     * 返回一个账号的所拥有的权限码集合
     * @param loginId
     * @param loginType
     * @return
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 判断 loginType，仅对类型为 "space" 进行权限校验
        if (!StpKit.SPACE_TYPE.equals(loginType)) {
            return new ArrayList<>();
        }
        // 管理员权限，表示权限校验通过
        List<String> ADMIN_PERMISSIONS = spaceUserAuthManager.getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        // 获取上下文对象
        SpaceUserAuthContext authContext = getAuthContextByRequest();
        // 如果所有字段都为空，表示查询公共图库，可以通过 这里没有登录也可以使用
        if (isAllFieldsNull(authContext)) {
            return ADMIN_PERMISSIONS;
        }
        // FIXME 这里出现bug 无法从StpKit中获取登录用户
        // 获取 userId
        User loginUser = (User) StpKit.SPACE.getSessionByLoginId(loginId).get(USER_LOGIN_STATE);

        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户未登录");
        }
        Long userId = loginUser.getId();
        // 优先从上下文中获取 SpaceUser 对象
        SpaceUser spaceUser = authContext.getSpaceUser();
        if (spaceUser != null) {
            return spaceUserAuthManager.getPermissionsByRole(spaceUser.getSpaceRole());
        }
        // 如果有 spaceUserId，必然是团队空间，通过数据库查询 SpaceUser 对象
        Long spaceUserId = authContext.getSpaceUserId();
        if (spaceUserId != null) {
            spaceUser = spaceUserApplicationService.getById(spaceUserId);
            if (spaceUser == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到空间用户信息");
            }

            // 取出当前登录用户对应的 spaceUser
            SpaceUser loginSpaceUser = spaceUserApplicationService.getSpaceUserBySpaceIdAndUserId(spaceUser.getSpaceId(), userId);
            if (loginSpaceUser == null) {
                return new ArrayList<>();
            }
            // 这里会导致管理员在私有空间没有权限，可以再查一次库处理
            return spaceUserAuthManager.getPermissionsByRole(loginSpaceUser.getSpaceRole());
        }
        // 如果没有 spaceUserId，尝试通过 spaceId 或 pictureId 获取 Space 对象并处理
        Long spaceId = authContext.getSpaceId();
        if (spaceId == null) {
            // 如果没有 spaceId，通过 pictureId 获取 Picture 对象和 Space 对象
            Long pictureId = authContext.getPictureId();
            // 图片 id 也没有，则默认通过权限校验
            if (pictureId == null) {
                return ADMIN_PERMISSIONS;
            }
            Picture picture =pictureApplicationService.getPictureByPictureId(pictureId);
            if (picture == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到图片信息");
            }
            spaceId = picture.getSpaceId();
            // 公共图库，仅本人或管理员可操作
            if (spaceId == null) {
                if (picture.getUserId().equals(userId) || loginUser.isAdmin()) {
                    return ADMIN_PERMISSIONS;
                } else {
                    // 不是自己的图片，仅可查看
                    return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
                }
            }
        }
        // 获取 Space 对象
        Space space = spaceApplicationService.getById(spaceId);
        if (space == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到空间信息");
        }
        // 根据 Space 类型判断权限
        if (space.getSpaceType() == SpaceTypeEnum.PRIVATE.getValue()) {
            // 私有空间，仅本人或管理员有权限
            if (space.getUserId().equals(userId) || loginUser.isAdmin()) {
                return ADMIN_PERMISSIONS;
            } else {
                return new ArrayList<>();
            }
        } else {
            // 团队空间，查询 SpaceUser 并获取角色和权限
            spaceUser = spaceUserApplicationService.getSpaceUserBySpaceIdAndUserId(spaceId,userId);
            if (spaceUser == null) {
                return new ArrayList<>();
            }
            return spaceUserAuthManager.getPermissionsByRole(spaceUser.getSpaceRole());
        }
    }

    /**
     *  返回一个账号所拥有的角色集合
     * @param loginId 登录id
     * @param loginType
     * @return
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    /**
     * 从请求中获取上下文
     * @return
     */
    public SpaceUserAuthContext getAuthContextByRequest(){
        // 获取请求
         HttpServletRequest request =  ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        // 获取请求类型 是get 还是post
        String contentType = request.getHeader(Header.CONTENT_TYPE.getValue());
        SpaceUserAuthContext authRequest;
        if(ContentType.JSON.getValue().equals(contentType)){
            // 如果是post
            String body = ServletUtil.getBody(request);
            authRequest = JSONUtil.toBean(body, SpaceUserAuthContext.class);
        }else{
            Map<String,String> paremMap = ServletUtil.getParamMap(request);
            authRequest = BeanUtil.toBean(paremMap,SpaceUserAuthContext.class);
        }
        // 根据请求路径区分id的不同含义
        Long id = authRequest.getId();
        if(ObjectUtil.isNotNull(id)){
            String requestURI = request.getRequestURI();
            // 替换掉api开头
            String pareURI=requestURI.replace(contextPath + "/" , "" );
            //获取前缀第一个字符段
            String  moduleName = StrUtil.subBefore(pareURI, "/", false);
            // 判断module的前缀是什么
            switch (moduleName){
                case "picture":
                    authRequest.setPictureId(id);
                    break;
                case "space":
                    authRequest.setSpaceId(id);
                    break;
                case "spaceUser":
                    authRequest.setSpaceUserId(id);
                default:
            }
        }
        return authRequest;
    
    }

    /**
     * 判断对象的所有字段是否为空
     * @param object
     * @return
     */
    private boolean isAllFieldsNull(Object object) {
        if (object == null) {
            // 对象本身为空
            return true;
        }
        // 获取所有字段并判断是否所有字段都为空
        return Arrays.stream(ReflectUtil.getFields(object.getClass()))
                // 获取字段值
                .map(field -> ReflectUtil.getFieldValue(object, field))
                // 检查是否所有字段都为空
                .allMatch(ObjectUtil::isEmpty);
    }

}
