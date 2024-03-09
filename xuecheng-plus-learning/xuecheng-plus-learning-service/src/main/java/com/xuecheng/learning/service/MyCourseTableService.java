package com.xuecheng.learning.service;

import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/3/5 17:41
 * @description 我的课程表服务
 **/
public interface MyCourseTableService {
    /**
     * @param userId   用户id
     * @param courseId 课程id
     * @return com.xuecheng.learning.model.dto.XcChooseCourseDto
     * @description 添加选课
     * @author Mr.M
     * @date 2022/10/24 17:33
     */
    XcChooseCourseDto addChooseCourse(String userId, Long courseId);

    /**
     * @param userId   用户id
     * @param courseId 课程id
     * @return com.xuecheng.learning.model.dto.XcCourseTablesDto
     * @description 查询学习资格，是否可以学习课程，是否已经选课，是否已经支付等等信息，返回给前端，前端根据这些信息决定是否可以学习课程，是否可以支付等等操作
     * @date 2022/10/24 17:33
     */
    XcCourseTablesDto getLearningStatus(String userId, Long courseId);


    /**
     * @param courseId 课程id
     * @return boolean
     * @description 保存选课成功记录，用于后续查询学习状态，是否已经选课等等
     */
    boolean saveChooseCourseSuccess(String courseId);

}
