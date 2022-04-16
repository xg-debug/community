package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MessageInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    // 在调用controller方法之后，返回模板之前执行(进行拦截)
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        // 判断用户有没有登录，这个数据要通过modelAndView携带,所以也要判断modelAndView是否为空
        if (user != null && modelAndView != null) {
            int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
            int noticeUnreadCount = messageService.selectNoticeUnreadCount(user.getId(), null);
            int allUnreadCount = letterUnreadCount + noticeUnreadCount;
//            System.out.println("===="+letterUnreadCount+"====");
//            System.out.println("===="+noticeUnreadCount+"====");
//            System.out.println("===="+allUnreadCount+"====");
            modelAndView.addObject("allUnreadCount", allUnreadCount);
        }

    }
}
