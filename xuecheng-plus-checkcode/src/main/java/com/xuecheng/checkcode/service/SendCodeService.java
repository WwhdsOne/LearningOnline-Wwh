package com.xuecheng.checkcode.service;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/3/4 19:49
 * @description 发送验证码服务
 **/
public interface SendCodeService {

        /**
        * 发送验证码
        * @param email 目标邮箱
        * @param code 验证码
        */
        void sendCodeToEmail(String email,String code);
}
