package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

         @Autowired
        private UserMapper userMapper;
    @Override
    public Result sendCode(String phone, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号码格式错误！");
        }
        //生成验证码
        String code = RandomUtil.randomNumbers(6);
        //保存到session
        session.setAttribute("code",code);
        //假的验证码log
        log.debug("发送短信验证码成功{}",code);
        return Result.ok();
    }

    @Override
    public Result longin(LoginFormDTO loginForm, HttpSession session) {

        //校验手机号
        if (RegexUtils.isPhoneInvalid(loginForm.getPhone())){
            return Result.fail("手机号码格式错误！");
        }
        //校验验证码
        Object code = session.getAttribute("code");
        if (code==null || !loginForm.getCode().equals(code)){
            return Result.fail("验证码错误");
        }
        //不一致报错

        //一致，根据手机号查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone,loginForm.getPhone());
        User user = userMapper.selectOne(queryWrapper);
        //判断用户是否存在
        if (user==null){
            //不存在，创建新用户并保存
           user=  createUserWithPhone(loginForm.getPhone());
        }

        //保存用户信息到session中
        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        return Result.ok();
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName("user_"+RandomUtil.randomString(10));
        save(user);
        return user;
    }
}
