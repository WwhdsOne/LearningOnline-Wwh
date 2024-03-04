package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.FindPswDto;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/3/4 20:02
 * @description 验证验证码服务
 **/
public interface VerifyService {

    void findPassword(FindPswDto findPswDto);
}
