package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

// 登录拦截器
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // handler为拦截的目标对象，如果是方法就拦截，否则就不拦截
        if (handler instanceof HandlerMethod) {
            // 类型转换
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 获取拦截到的method对象
            Method method = handlerMethod.getMethod();
            // 取该方法上的注解，这里是@LoginRequired
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            if (loginRequired!=null && hostHolder.getUser() == null) {
                // 如果没有登录就重定向到登录页面
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }

        }
        return true;
    }
}
