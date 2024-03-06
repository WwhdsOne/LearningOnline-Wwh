package com.xuecheng.orders.service;

import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/3/6 17:59
 * @description 订单服务接口
 **/
public interface OrderService {
    /**
     * 创建订单
     * @param userId 用户id
     * @param addOrderDto 订单信息
     * @return 订单id
     */
    PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);
}
