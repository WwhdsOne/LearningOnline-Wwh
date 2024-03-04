package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.FindPswDto;
import com.xuecheng.ucenter.model.dto.RegisterDto;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.VerifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

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
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private XcUserMapper userMapper;

    @Override
    public void findPassword(FindPswDto findPswDto) {
        String email = findPswDto.getEmail();
        String checkcode = findPswDto.getCheckcode();
        Boolean verify = verify(email, checkcode);
        if ( !verify ) {
            throw new RuntimeException("验证码输入错误");
        }
        String password = findPswDto.getPassword();
        String confirmpwd = findPswDto.getConfirmpwd();
        if ( !password.equals(confirmpwd) ) {
            log.info("两次密码不一致");
            XueChengPlusException.cast("两次密码不一致");
        }
        //修改密码
        LambdaQueryWrapper<XcUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(XcUser::getEmail, findPswDto.getEmail());
        wrapper.eq(XcUser::getCellphone, findPswDto.getCellphone());
        XcUser xcUser = userMapper.selectOne(wrapper);
        if ( xcUser == null ) {
            log.info("用户不存在");
            XueChengPlusException.cast("用户不存在");
        }
        xcUser.setPassword(new BCryptPasswordEncoder().encode(password));
        userMapper.updateById(xcUser);
    }

    @Autowired
    private XcUserRoleMapper userRoleMapper;

    public Boolean verify(String email, String checkcode) {
        // 1. 从redis中获取缓存的验证码
        String codeInRedis = redisTemplate.opsForValue().get(email);
        // 2. 判断是否与用户输入的一致
        if ( codeInRedis != null && codeInRedis.equalsIgnoreCase(checkcode) ) {
            redisTemplate.delete(email);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void register(RegisterDto registerDto) {
        String id = UUID.randomUUID().toString();
        String email = registerDto.getEmail();
        String password = registerDto.getPassword();
        String confirmpwd = registerDto.getConfirmpwd();
        String checkcode = registerDto.getCheckcode();
        Boolean verify = verify(email, checkcode);
        //验证码错误
        if ( !verify ) {
            throw new RuntimeException("验证码输入错误");
        }
        //两次密码不一致
        if ( !password.equals(confirmpwd) ) {
            throw new RuntimeException("两次密码不一致");
        }
        LambdaQueryWrapper<XcUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(XcUser::getEmail, email);
        XcUser xcUser = userMapper.selectOne(wrapper);
        if ( xcUser != null ) {
            throw new RuntimeException("邮箱已被注册,一个账号只能有一个用户");
        }
        XcUser user = new XcUser();
        BeanUtils.copyProperties(registerDto, user);
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        user.setId(id);
        user.setUtype("101001");
        user.setStatus("1");
        user.setName(registerDto.getNickname());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        int insert = userMapper.insert(user);
        if ( insert <= 0 ) {
            XueChengPlusException.cast("注册失败");
        }
        XcUserRole userRole = new XcUserRole();
        userRole.setUserId(id);
        userRole.setRoleId("17");
        userRole.setId(id);
        userRole.setCreateTime(LocalDateTime.now());
        int insert1 = userRoleMapper.insert(userRole);
        if ( insert1 <= 0 ) {
            XueChengPlusException.cast("注册失败");
        }
    }
}
