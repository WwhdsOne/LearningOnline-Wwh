package com.xuecheng.checkcode.service.impl;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.checkcode.service.SendCodeService;
import com.xuecheng.checkcode.utils.MailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/3/4 19:51
 * @description 发送验证码服务实现
 **/
@Service
@Slf4j
public class SendCodeServiceImpl implements SendCodeService {

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Override
    public void sendCodeToEmail(String email, String code) {
        log.info("发送验证码到邮箱：{}，验证码：{}", email, code);
        try {
            //1.发送邮件
            MailUtil.sendTestMail(email, code);
        } catch (MessagingException e) {
            log.info("发送验证码到邮箱失败：{}，验证码：{}", email, code);
            XueChengPlusException.cast("发送验证码到邮箱失败");
        }
        //2.将验证码存入redis
        long CODE_EXPIRE_TIME = 2 * 60L;
        redisTemplate.opsForValue().set(email, code, CODE_EXPIRE_TIME);
    }
}
