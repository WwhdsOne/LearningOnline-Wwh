package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.po.XcUser;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/3/2 18:42
 * @description 微信认证服务
 **/
public interface WxAuthService {
    /**
     * 微信认证
     * 1.申请令牌
     * 2.携带令牌查询用户信息
     * 3.将用户信息传入数据库
     * @param code 微信code
     * @return XcUser
     */
    XcUser wxAuth(String code);
}
