package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.FindPswDto;
import com.xuecheng.ucenter.model.dto.RegisterDto;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/3/4 20:02
 * @description 验证验证码服务
 **/
public interface VerifyService {

    /**
     * 验证验证码
     * @param findPswDto 找回密码dto
     */
    void findPassword(FindPswDto findPswDto);
    /**
     * 注册
     * @param registerDto 注册dto
     */
    void register(RegisterDto registerDto);
}
