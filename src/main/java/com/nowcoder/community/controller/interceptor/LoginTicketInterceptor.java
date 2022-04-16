package com.nowcoder.community.controller.interceptor;


import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    /**
     * 在controller执行之前，进行拦截，获取用户的登陆凭证，对用户进行授权
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从cookie中获取凭证
        String ticket = CookieUtil.getValue(request,"ticket");
        if (ticket != null) {
            // 根据ticket中的userId查询用户
            // 先查询凭证，检查凭证是否有效
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            if (loginTicket != null && loginTicket.getStatus() == 0 &&loginTicket.getExpired().after(new Date())) {
                // 当前时间在超时时间之前为有效
                // 查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                // 在本次请求中持有用户,要考虑多线程高并发
                // 将user暂存到当前线程的对象中
                hostHolder.setUser(user);

                // 构建用户认证的结果，并存入到securityContext，以便于security进行授权
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user, user.getPassword(), userService.getAuthorities(user.getId())
                );

                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));

            }

        }
        return true;
    }

    /**
     * 在controller执行之后，进行拦截，保存登陆用户的信息
     * 它的执行时间是在处理器进行处理之后，也就是在Controller的方法调用之后执行，
     * 但是它会在DispatcherServlet进行视图的渲染之前执行，也就是说在这个方法中你可以对ModelAndView进行操作。
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser",user);
        }
    }

    /**
     * 该方法将在整个请求完成之后，也就是DispatcherServlet渲染了视图执行。
     * 这个方法的主要作用是用于清理资源
     * SecurityContextHolder.clearContext();清丽的话，会出现登录之后访问系统通知页面时又要求重新进行登录的情况
     * 这是因为你最开始执行登录请求的时候，在login整个请求执行完后，afterCompletion()会清理用户的认证，
     * 而访问系统通知页面需要用户认证后才能访问
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
        //SecurityContextHolder.clearContext();
    }
}
