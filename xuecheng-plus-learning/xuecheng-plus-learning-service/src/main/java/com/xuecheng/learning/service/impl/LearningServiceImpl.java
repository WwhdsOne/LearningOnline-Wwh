package com.xuecheng.learning.service.impl;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.base.utils.StringUtil;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.feignclient.MediaServiceClient;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.service.MyCourseTableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/3/10 16:14
 * @description 在线学习相关接口实现
 **/
@Service
@Slf4j
public class LearningServiceImpl implements LearningService {

    @Autowired
    MyCourseTableService myCourseTableService;

    @Autowired
    ContentServiceClient contentServiceClient;

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {
        //查询课程信息
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if ( coursepublish == null ) {
            return RestResponse.validfail("课程信息不存在");
        }
        //远程调用内容管理服务根据课程计划ID查询课程计划,如果is_preview为1则支持试学
        //也可以从coursepublish解析出课程计划是否支持试学

        //1.判断是否登录
        if ( StringUtil.isNotEmpty(userId) ) {

            //获取学习资格
            //学习资格，[{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
            XcCourseTablesDto learning = myCourseTableService.getLearningStatus(userId, courseId);
            String learnStatus = learning.getLearnStatus();
            if ( "702002".equals(learnStatus) ) {
                return RestResponse.validfail("无法学习,没有选课或选课后没有支付");
            } else if ( "702003".equals(learnStatus) ) {
                return RestResponse.validfail("无法学习,已过期需要申请续期或重新支付");
            } else {
                //远程调用媒资获取视频播放地址
                return mediaServiceClient.getPlayUrlByMediaId(mediaId);
            }
        }
        //2.如果用户没有登录
        //查询课程信息判断是否为免费课程
        String charge = coursepublish.getCharge();
        if ( "201000".equals(charge) ) {
            //免费课程
            //远程调用媒资获取视频播放地址
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }
        return RestResponse.validfail("课程需要购买");
    }
}
