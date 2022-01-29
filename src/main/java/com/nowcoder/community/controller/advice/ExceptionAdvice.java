package com.nowcoder.community.controller.advice;

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常：" + e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }

        String xRequestWith = request.getHeader("x-request-with");
        if ("XMLHttpRequest".equals(xRequestWith)) {
            // 请求方式为异步请求
            response.setContentType("application/plain;charset = utf-8");
            PrintWriter out = response.getWriter();
            out.write(CommunityUtil.getJSONString(1,"服务器异常"));
        } else {
            // 请求方式为普通方式
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
