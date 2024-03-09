package com.xuecheng.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.learning.config.PayNotifyConfig;
import com.xuecheng.learning.service.MyCourseTableService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/3/9 17:11
 * @description 支付通知服务
 **/
@Service
@Slf4j
public class ReceivePayNotifyService {

    @Autowired
    private MyCourseTableService myCourseTableService;
    @RabbitListener(queues = PayNotifyConfig.PAYNOTIFY_QUEUE)
    public void receivePayNotify(Message message) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("接收到支付通知消息：{}", message);
        byte[] body = message.getBody();
        String msg = new String(body);
        //转为对象
        MqMessage mqMessage = JSON.parseObject(msg, MqMessage.class);
        //解析消息内容

        //选课ID
        String chooseCourseId = mqMessage.getBusinessKey1();

        //获取订单类型
        String orderType = mqMessage.getBusinessKey2();

        //学习中心类服务只要购买课程的支付订单结果
        if("60201".equals(orderType)){
            //支付成功
            //根据消息内容,更新选课记录,像我的课程表添加课程
            boolean b = myCourseTableService.saveChooseCourseSuccess(chooseCourseId);
            if(!b){
                XueChengPlusException.cast("保存选课记录失败");
            }
        }
    }
}
