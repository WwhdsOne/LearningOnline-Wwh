package com.xuecheng.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.utils.IdWorkerUtils;
import com.xuecheng.base.utils.QRCodeUtil;
import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.mapper.XcOrdersMapper;
import com.xuecheng.orders.mapper.XcPayRecordMapper;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/3/6 17:59
 * @description 订单服务实现类
 **/
@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    private XcOrdersMapper xcOrdersMapper;

    @Autowired
    private XcOrdersGoodsMapper xcOrdersGoodsMapper;

    @Autowired
    private XcPayRecordMapper xcPayRecordMapper;

    @Value("${pay.qrcodeurl}")
    private String qrcodeUrl;

    @Transactional
    @Override
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) {


        //插入订单主表,订单明细表
        XcOrders xcOrders = saveXcOrders(userId, addOrderDto);

        //插入支付记录
        XcPayRecord xcPayRecord = createPayRecord(xcOrders);
        Long payNo = xcPayRecord.getPayNo();

        //生成支付二维码
        QRCodeUtil qrCodeUtil = new QRCodeUtil();
        String url = String.format(qrcodeUrl, payNo);
        String qrCode = null;
        try {
            qrCode = qrCodeUtil.createQRCode(url, 200, 200);
        } catch (IOException e) {
            XueChengPlusException.cast("生成支付二维码失败");
        }
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(xcPayRecord, payRecordDto);
        payRecordDto.setQrcode(qrCode);//二维码

        return payRecordDto;
    }

    /**
     * 插入支付记录
     * @param orders 订单信息
     * @return 支付记录
     */
    private XcPayRecord createPayRecord(XcOrders orders) {
        //订单ID
        Long orderId = orders.getId();
        //如果订单不存在,不添加支付记录
        XcOrders xcOrders = xcOrdersMapper.selectById(orderId);
        if(xcOrders == null){
            XueChengPlusException.cast("订单不存在");
        }
        //如果订单支付已成功,也不添加支付记录
        String status = xcOrders.getStatus();
        if("601002".equals(status)){
            XueChengPlusException.cast("订单已支付");
        }
        //插入支付记录
        XcPayRecord xcPayRecord = new XcPayRecord();
        xcPayRecord.setPayNo(IdWorkerUtils.getInstance().nextId());//雪花算法生成支付号
        xcPayRecord.setOrderId(orderId);
        xcPayRecord.setOrderName(xcOrders.getOrderName());
        xcPayRecord.setTotalPrice(xcOrders.getTotalPrice());
        xcPayRecord.setCurrency("CNY");
        xcPayRecord.setCreateDate(LocalDateTime.now());
        xcPayRecord.setStatus("601001");//支付类型:未支付
        xcPayRecord.setUserId(xcOrders.getUserId());
        int insert = xcPayRecordMapper.insert(xcPayRecord);
        if(insert <= 0){
            XueChengPlusException.cast("插入支付记录失败");
        }
        //返回支付记录
        return xcPayRecord;
    }

    // 保存订单
    public XcOrders saveXcOrders(String userId, AddOrderDto addOrderDto) {
        // 插入订单表

        // 进行幂等性判断,确定每一个选课记录只能有一个订单
        XcOrders xcOrdersByBusinessId = getXcOrdersByBusinessId(addOrderDto.getOutBusinessId());
        if(xcOrdersByBusinessId != null){
            return xcOrdersByBusinessId;
        }
        // 1.插入订单主表
        XcOrders xcOrders = new XcOrders();
        xcOrders.setId(IdWorkerUtils.getInstance().nextId()); //雪花算法生成订单号
        xcOrders.setTotalPrice(addOrderDto.getTotalPrice());
        xcOrders.setCreateDate(LocalDateTime.now());
        xcOrders.setStatus("600001");//未支付
        xcOrders.setUserId(userId);
        xcOrders.setOrderType("60201");//订单类型:课程
        xcOrders.setOrderName(addOrderDto.getOrderName());
        xcOrders.setOrderDescrip(addOrderDto.getOrderDescrip());
        xcOrders.setOrderDetail(addOrderDto.getOrderDetail());
        xcOrders.setOutBusinessId(addOrderDto.getOutBusinessId());//如果是购买课程此处是选课的ID
        int insert = xcOrdersMapper.insert(xcOrders);
        if(insert <= 0){
            XueChengPlusException.cast("插入订单失败");
        }
        Long orderId = xcOrders.getId();
        // 2.插入订单明细表
        String orderDetailJSON = addOrderDto.getOrderDetail();
        //解析JSON
        List<XcOrdersGoods> xcOrdersGoods = JSON.parseArray(orderDetailJSON, XcOrdersGoods.class);
        //遍历插入
        for (XcOrdersGoods xcOrdersGoods1 : xcOrdersGoods) {
            xcOrdersGoods1.setOrderId(orderId);
            int insert1 = xcOrdersGoodsMapper.insert(xcOrdersGoods1);
            if(insert1 <= 0){
                XueChengPlusException.cast("插入订单明细失败");
            }
        }
        return xcOrders;
    }

    /**
     * 根据业务ID查询订单
     * @param outBusinessId 业务ID
     * @return 订单信息
     */
    public XcOrders getXcOrdersByBusinessId(String outBusinessId) {
        //根据业务ID查询订单
        return xcOrdersMapper.selectOne(new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getOutBusinessId, outBusinessId));
    }
}
