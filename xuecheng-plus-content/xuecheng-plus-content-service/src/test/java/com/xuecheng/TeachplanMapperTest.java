package com.xuecheng;

import com.xuecheng.content.mapper.TeachplanMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/12 19:45
 * @description 课程计划 Mapper 接口测试
 **/
@SpringBootTest
class TeachplanMapperTest {

    @Autowired
    TeachplanMapper teachplanMapper;
    @Test
    void selectTreeNodes() {
        teachplanMapper.selectTreeNodes(117L).forEach(System.out::println);
    }
}
