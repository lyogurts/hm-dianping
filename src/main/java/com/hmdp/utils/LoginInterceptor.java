package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import com.hmdp.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginInterceptor implements HandlerInterceptor {
   //接受传递过来的redisTemplate
    private StringRedisTemplate redisTemplate;

    public LoginInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate=redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.获取token
        String token = request.getHeader("authorization");
        if (!StringUtils.hasText(token)){
            //不存在，拦截 返回401状态码
            response.setStatus(401);
            return false;
        }
        //2.获取redis中存储的数据
        Map<Object, Object> userMap = redisTemplate.opsForHash().entries(RedisConstants.LOGIN_USER_KEY + token);
        //3.判断用户是否存在
        if (userMap.isEmpty()){
            //不存在，拦截 返回401状态码
            response.setStatus(401);
            return false;
        }
        //4.将查询到的hash转换为UserDTO
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);

        //5.存在，保存用户信息到ThreadLocal
        UserHolder.saveUser(userDTO);
        //设置有效期(实则更新token)
        redisTemplate.expire(RedisConstants.LOGIN_USER_KEY+token,RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        //放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
