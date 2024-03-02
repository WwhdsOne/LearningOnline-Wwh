package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/3/2 10:08
 * @description 用户服务实现类
 **/
@Component
@Slf4j
public class UserServiceImpl implements UserDetailsService {

    //用户中心dao层
    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired
    ApplicationContext applicationContext;

    //传入请求对象是authParamDTO

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        //将传入json转换为authParamDTO对象
        AuthParamsDto authParamsDto = null;
        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            throw new RuntimeException("请求认证参数不符合请求");
        }
        //获取认证类型
        String authType = authParamsDto.getAuthType();
        //根据认证类型获取对应的认证服务
        String beanName = authType + "_authService";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);
        //执行认证
        XcUserExt xcUserExt = authService.execute(authParamsDto);
        UserDetails userDetails = getUserPrincipal(xcUserExt);
        return userDetails;
    }

    /**
     * 根据xcUserExt构建UserDetails对象
     *
     * @param xcUserExt 用户信息
     * @return UserDetails
     */
    private UserDetails getUserPrincipal(XcUserExt xcUserExt) {
        String password = xcUserExt.getPassword();
        //权限
        String[] authorities = {"test"};
        //将用户信息转json
        String userJson = JSON.toJSONString(xcUserExt);
        UserDetails userDetails = User
                .builder()
                .username(userJson)
                .password(password)
                .authorities(authorities).build();
        return userDetails;
    }
}
