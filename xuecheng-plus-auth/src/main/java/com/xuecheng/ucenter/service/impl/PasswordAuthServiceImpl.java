package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/3/2 11:27
 * @description 密码认证服务实现类
 **/
@Service("password_authService")
public class PasswordAuthServiceImpl implements AuthService {

    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        //账号
        String username = authParamsDto.getUsername();

        //验证码 TODO


        //查询数据库
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));

        //查询用户不存在,返回null即可,spring security同时抛出异常提示用户不存在
        if ( xcUser == null ) {
            throw new RuntimeException("账号不存在");
        }
        //验证密码是否正确
        //如果查到了正确的用户拿到了正确的密码,返回UserDetails对象给spring security框架,由框架进行密码比对
        //数据库密码
        String passwordDB = xcUser.getPassword();
        //用户输入密码
        String passwordForm = authParamsDto.getPassword();

        boolean matches = passwordEncoder.matches(passwordForm, passwordDB);
        if ( !matches ) {
            throw new RuntimeException("账号或密码错误");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);
        return xcUserExt;
    }
}
