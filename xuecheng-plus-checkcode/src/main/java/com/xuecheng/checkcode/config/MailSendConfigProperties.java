package com.xuecheng.checkcode.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/3/5 9:14
 * @description 邮件发送配置
 **/


@Configuration
public class MailSendConfigProperties {


    @Value(value = "${spring.mail.host}")
    public String host;

    @Value(value = "${spring.mail.port}")
    public String port;

    @Value(value = "${spring.mail.username}")
    public String username;

    @Value(value = "${spring.mail.password}")
    public String password;
}
