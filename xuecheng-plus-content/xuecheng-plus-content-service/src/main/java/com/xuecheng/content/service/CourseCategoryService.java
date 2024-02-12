package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryDTO;

import java.util.List;

public interface CourseCategoryService {
    /**
     * 课程分类树形结构查询
     *
     * @return
     */
    List<CourseCategoryDTO> queryTreeNodes(String id);
}
