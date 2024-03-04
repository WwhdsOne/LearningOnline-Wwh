package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.FindPswDto;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.VerifyService;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.provider.keystore.BC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/3/4 20:04
 * @description 验证验证码服务
 **/
@Service
@Slf4j
public class VerifyServiceImpl implements VerifyService {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private XcUserMapper userMapper;
    @Override
    public void findPassword(FindPswDto findPswDto) {
        //获取redis中的验证码
        String code = redisTemplate.opsForValue().get(findPswDto.getEmail());
        if (code == null) {
            log.info("验证码已过期");
            XueChengPlusException.cast("验证码已过期");
        }
        if (!code.equals(findPswDto.getCheckcode())) {
            log.info("验证码错误");
            XueChengPlusException.cast("验证码已过期");
        }
        String password = findPswDto.getPassword();
        String confirmpwd = findPswDto.getConfirmpwd();
        if (!password.equals(confirmpwd)) {
            log.info("两次密码不一致");
            XueChengPlusException.cast("两次密码不一致");
        }
        //修改密码
        LambdaQueryWrapper<XcUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(XcUser::getEmail,findPswDto.getEmail());
        wrapper.eq(XcUser::getCellphone,findPswDto.getCellphone());
        XcUser xcUser = userMapper.selectOne(wrapper);
        if (xcUser == null) {
            log.info("用户不存在");
            XueChengPlusException.cast("用户不存在");
        }
        xcUser.setPassword(new BCryptPasswordEncoder().encode(password));
        userMapper.updateById(xcUser);
    }
}
