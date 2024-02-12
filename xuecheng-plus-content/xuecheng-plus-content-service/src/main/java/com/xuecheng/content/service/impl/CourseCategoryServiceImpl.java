package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryDTO;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/11 20:00
 * @description 课程分类查询
 **/
@Service
@Slf4j
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryDTO> queryTreeNodes(String id) {
        //调用mapper查询分类信息
        List<CourseCategoryDTO> courseCategoryDTOS = courseCategoryMapper.selectTreeNodes(id);
        //封装成list类型返回
        //将list转换成map,key为id,value为CourseCategoryDTO
        Map<String, CourseCategoryDTO> map = courseCategoryDTOS.stream()
                .collect(Collectors.toMap(CourseCategory::getId, value -> value, (key1, key2) -> key2));
        //遍历list,查找collect子节点
        List<CourseCategoryDTO> result = new ArrayList<>();
        courseCategoryDTOS.stream().filter(item -> !id.equals(item.getId())) //去除根节点
                .forEach(item -> {
                    if ( item.getParentid().equals(id) ) {
                        result.add(item);
                    }
                    CourseCategoryDTO parent = map.get(item.getParentid());
                    //父节点属于要要找的节点则此时会在map中,若不是要找的节点则会被filter过滤
                    if ( parent != null ) {
                        //如果该父节点的子节点集合为空,设置一个新的集合
                        if ( parent.getChildrenTreeNodes() == null ) {
                            parent.setChildrenTreeNodes(new ArrayList<>());
                        }
                        parent.getChildrenTreeNodes().add(item);
                    }
                });
        return result;
    }
}
