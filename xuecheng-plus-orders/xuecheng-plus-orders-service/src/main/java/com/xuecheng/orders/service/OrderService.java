package com.xuecheng.orders.service;

import com.alipay.api.AlipayApiException;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcPayRecord;

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
    /**
     * 根据支付流水号查询支付记录
     * @param payNo 支付流水号
     * @return 支付记录
     */
    XcPayRecord getPayRecordByPayno(String payNo) throws AlipayApiException;
    /**
     * 查询支付结果
     * @param payNo 支付流水号
     * @return 支付结果
     */
    PayRecordDto queryPayResult(String payNo) throws AlipayApiException;
    /**
     * 保存支付宝支付状态
     * @param payStatusDto 支付状态
     */
    void saveAliPayStatus(PayStatusDto payStatusDto);
}
