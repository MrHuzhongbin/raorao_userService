package com.itmd.user.filter;

import com.itmd.auth.entiy.UserInfo;
import com.itmd.auth.utils.JwtUtils;
import com.itmd.user.config.JwtProperties;
import com.itmd.user.utils.CookieUtils;
import com.rabbitmq.tools.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class LoginInterceptor extends HandlerInterceptorAdapter {

    private static final ThreadLocal<UserInfo> tl = new InheritableThreadLocal<>();
    private JwtProperties prop;
    public LoginInterceptor(JwtProperties prop) {
        this.prop = prop;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取cookie中的token
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        try {
            //解析token
            UserInfo userinfo = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            //传递user
            tl.set(userinfo);
            return true;
        } catch (Exception e) {
            log.info("用户中心：解析用户身份失败");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("未授权！");
            return false;
        }
    }
    public static UserInfo getUser(){
        return tl.get();
    }
}
