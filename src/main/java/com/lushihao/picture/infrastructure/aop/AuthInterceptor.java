package com.lushihao.picture.infrastructure.aop;

import com.lushihao.picture.infrastructure.annotation.AuthCheck;
import com.lushihao.picture.infrastructure.exception.BusinessException;
import com.lushihao.picture.infrastructure.exception.ErrorCode;
import com.lushihao.picture.domain.user.entity.User;
import com.lushihao.picture.domain.user.valueobject.UserRoleEnum;
import com.lushihao.picture.application.service.UserApplicationService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用于权限校验的AOP
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-27   19:51
 */

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserApplicationService userApplicationService;

    /**
     * 拦截所有带有AuthCheck注解的方法
     * @param joinPoint 切入点
     * @param authCheck 用于获取权限校验值
     */
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 从authCheck中获得当前必须有的权限
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 获取当前登录用户
        User loginUser  = userApplicationService.getLoginUser(request);
        // 从mustRole value值获取当前的UserRoleEnum对象
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        // 如果当前方法所需权限为空 直接放行
        if(mustRoleEnum==null ){
            return joinPoint.proceed();
        }
        // 下面获取用户的权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        // 如果用户权限为空 那报错
        if(userRoleEnum == null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 如果要求是管理员的权限 但是用户不是管理员 那就报错
        if(UserRoleEnum.Admin.equals(mustRoleEnum) && !UserRoleEnum.Admin.equals(userRoleEnum)) {
            throw  new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return joinPoint.proceed();
    }

}
