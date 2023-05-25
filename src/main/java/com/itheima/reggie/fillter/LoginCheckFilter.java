package com.itheima.reggie.fillter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否完成登入
 */
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest) servletRequest;
        HttpServletResponse response=(HttpServletResponse) servletResponse;
        //1.获取本地的请求
        String requestURI = request.getRequestURI();
        //定义不需要处理的请求路径
        String[] urls=new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/sendMsg",
                "/user/login"
        };
        boolean check = check(urls, requestURI);
        //1.如果不需要拦截放行
        if(check){
            log.info("不需要拦截{}",requestURI);
            filterChain.doFilter(request,response);
            return;
        }
        //2.如果已登录直接放行
        if(request.getSession().getAttribute("employee")!=null){
            log.info("用户已登录{}",requestURI);
            BaseContext.setCurrentId((long)request.getSession().getAttribute("employee"));
            filterChain.doFilter(request,response);
            return;
        }
        if(request.getSession().getAttribute("user")!=null){
            log.info("用户已登录{}",requestURI);
            BaseContext.setCurrentId((long)request.getSession().getAttribute("user"));
            filterChain.doFilter(request,response);
            return;
        }
        //3.未登录
        log.info("用户未登录{}",requestURI);
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }
    public boolean check(String[] urls,String requestURI){
        for(String url:urls){
            boolean match=PATH_MATCHER.match(url,requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
