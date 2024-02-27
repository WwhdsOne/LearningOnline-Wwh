package com.xuecheng.content.service.jobhandler;

import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/27 17:05
 * @description 课程发布任务类
 **/
@Component
@Slf4j
public class CoursePublishTask extends MessageProcessAbstract {

    @XxlJob("CoursePublishJobHandler")
    public void coursePublish() throws Exception{
        //分片参数
        int shardIndex = XxlJobHelper.getShardIndex();  //执行器的序号, 从0开始编号
        int shardTotal = XxlJobHelper.getShardTotal();  //执行器的总数
        //调用抽象类执行任务
        /*
         * @description 扫描消息表多线程执行任务
         * @param shardIndex 分片序号
         * @param shardTotal 分片总数
         * @param messageType  消息类型
         * @param count  一次取出任务总数
         * @param timeout 预估任务执行时间,到此时间如果任务还没有结束则强制结束 单位秒
         * @return void
         * @author Mr.M
         * @date 2022/9/21 20:35
         */
        process(shardIndex, shardTotal, "course_publish", 30, 60);
    }
    /**
     * 执行课程发布的逻辑
     * @param mqMessage 消息
     * @return 是否成功
     */
    //若抛出异常，会说明任务失败
    @Override
    public boolean execute(MqMessage mqMessage) {
        //获取消息相关业务信息
        String businessKey1 = mqMessage.getBusinessKey1();
        long courseId = Long.parseLong(businessKey1);

        //课程静态化上传到minio(对应阶段1)
        generateCourseHtml(mqMessage, courseId);


        //向elasticsearch写数据(对应阶段2)
        saveCourseIndex(mqMessage, courseId);



        //向redis写缓存(对应阶段3)


        //返回true表示任务完成
        return true;
    }

    //生成课程静态化页面并上传至文件系统
    public void generateCourseHtml(MqMessage mqMessage, long courseId) {
        log.info("开始执行课程静态化任务,id:{}", mqMessage.getId());
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        //任务幂等性处理
        //取出当前阶段执行状态
        int stageOne = mqMessageService.getStageOne(taskId);
        if(stageOne > 0){
            //已经执行过了
            log.info("课程静态化完成,无需处理");
            return ;
        }

        //开始进行课程静态化


        //将任务状态设置为完成
        mqMessageService.completedStageOne(taskId);
    }

    //向elasticsearch写数据
    private void saveCourseIndex(MqMessage mqMessage, long courseId) {
        log.info("开始执行课程索引任务,id:{}", mqMessage.getId());
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        //任务幂等性处理
        //取出当前阶段执行状态
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if(stageTwo > 0){
            //已经执行过了
            log.info("课程索引已完成,无需处理");
            return ;
        }

        //开始向elasticsearch写数据


        //将任务状态设置为完成
        mqMessageService.completedStageTwo(taskId);
    }


}
