package com.nowcoder.community.config;

import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {
    @Override
    public void configure(WebSecurity web) throws Exception {
        // 设置所有 resource 之下的静态资源都可以访问
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 授权，对于这些路径，只要你拥有以下任何权限就允许访问。
        http.authorizeRequests().antMatchers(
                "/user/setting",
                "/user/upload",
                "/discuss/add",
                "/comment/add/**",
                "/letter/**",
                "/notice/**",
                "/follow",
                "/unfollow",
                "/like"
        ).hasAnyAuthority(
                AUTHORITY_USER,
                AUTHORITY_ADMIN,
                AUTHORITY_MODERATOR
        ).antMatchers(
                "/discuss/top",
                "/discuss/wonderful"
        ).hasAnyAuthority(
                AUTHORITY_MODERATOR
        ).antMatchers(
                "/discuss/delete",
                "/data/**",
                "/actuator/**"
        ).hasAnyAuthority(
                AUTHORITY_ADMIN
        ).anyRequest().permitAll().and().csrf().disable();

        // 权限不足
        http.exceptionHandling().authenticationEntryPoint(new AuthenticationEntryPoint() {
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                // 没有登录时的处理
                // 如果当前请求是一个普通请求，就直接跳转重定向到登陆页面
                // 如果当前请求是一个异步请求，就返回一个json字符串格式的提示
                // 判断请求是否是异步请求，可以通过获取请求头的参数来判断
                String xRequestedWith = request.getHeader("x-requested-with");
                if ("XMLHttpRequest".equals(xRequestedWith)) {
                    response.setContentType("application/plain;charset=utf-8");
                    PrintWriter writer = response.getWriter();
                    writer.write(CommunityUtil.getJSONString(403, "你还没有登录哦!"));
                } else {
                    response.sendRedirect(request.getContextPath() + "/login");
                }
            }
        }).accessDeniedHandler(new AccessDeniedHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                // 权限不足时的处理
                String xRequestedWith = request.getHeader("x-requested-with");
                if ("XMLHttpRequest".equals(xRequestedWith)) {
                    response.setContentType("application/plain;charset=utf-8");
                    PrintWriter writer = response.getWriter();
                    writer.write(CommunityUtil.getJSONString(403, "你没有访问此功能的权限!"));
                } else {
                    response.sendRedirect(request.getContextPath() + "/denied");
                }
            }
        });

        // security底层默认会拦截/logout请求，进行退出处理
        // 覆盖它默认的逻辑，才能执行我们自己的代码
        // 随便设置一个退出的路径，这样就能执行我们自己的代码逻辑
        http.logout().logoutUrl("/securitylogout");
    }

    // 认证会执行loginController自己写逻辑
}
