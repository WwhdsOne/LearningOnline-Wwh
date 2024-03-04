package com.xuecheng;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/3/4 23:56
 * @description Redis测试
 **/
@SpringBootTest
public class RedisTest {

    private RedisTemplate<String,String> redisTemplate;
    @Test
    public void test() {
        String s = redisTemplate.opsForValue().get("a1605691832@163.com");
        System.out.println(s);
    }

    @Test
    public void testall() {
        redisTemplate.keys("*").forEach(System.out::println);
    }
}
