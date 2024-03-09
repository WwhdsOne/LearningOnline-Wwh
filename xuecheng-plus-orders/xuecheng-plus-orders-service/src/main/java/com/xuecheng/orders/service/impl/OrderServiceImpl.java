package com.xuecheng.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.utils.IdWorkerUtils;
import com.xuecheng.base.utils.QRCodeUtil;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xuecheng.orders.config.AlipayConfig;
import com.xuecheng.orders.config.PayNotifyConfig;
import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.mapper.XcOrdersMapper;
import com.xuecheng.orders.mapper.XcPayRecordMapper;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/3/6 17:59
 * @description 订单服务实现类
 **/
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {


    @Autowired
    private XcOrdersMapper xcOrdersMapper;

    @Autowired
    private XcOrdersGoodsMapper xcOrdersGoodsMapper;

    @Autowired
    private XcPayRecordMapper xcPayRecordMapper;

    @Autowired
    private OrderService currentProxy;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    MqMessageService mqMessageService;

    @Value("${pay.qrcodeurl}")
    private String qrcodeUrl;


    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

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

    @Override
    public XcPayRecord getPayRecordByPayno(String payNo) {
        return xcPayRecordMapper.selectOne(new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
    }


    /**
     * 请求支付宝查询支付结果
     * @param payNo 支付交易号
     * @return 支付结果
     */
    public PayStatusDto queryPayResultFromAlipay(String payNo) throws AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi-sandbox.dl.alipaydev.com/gateway.do", APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payNo);           //out_trade_no和trade_no二选一即可
//bizContent.put("trade_no", "2014112611001004680073956707");
        request.setBizContent(bizContent.toString());
        String resultJson = null;
        try{
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            //交易不成功
            if(!response.isSuccess()){
                XueChengPlusException.cast("交易不成功");
            }
            resultJson = response.getBody();
        }catch (AlipayApiException e){
            e.printStackTrace();
            XueChengPlusException.cast("调用支付宝查询接口失败");
        }
        //转map
        Map resultMap = JSON.parseObject(resultJson, Map.class);
        Map alipay_trade_query_response = (Map) resultMap.get("alipay_trade_query_response");
        //支付结果
        String trade_status = (String) alipay_trade_query_response.get("trade_status");
        String total_amount = (String) alipay_trade_query_response.get("total_amount");
        String trade_no = (String) alipay_trade_query_response.get("trade_no");
        //保存支付结果
        PayStatusDto payStatusDto = new PayStatusDto();
        payStatusDto.setOut_trade_no(payNo);
        payStatusDto.setTrade_status(trade_status);
        payStatusDto.setApp_id(APP_ID);
        payStatusDto.setTrade_no(trade_no);
        payStatusDto.setTotal_amount(total_amount);
        return payStatusDto;
    }

    /**
     * @description 保存支付宝支付结果
     * @param payStatusDto  支付结果信息
     * @return void
     * @author Mr.M
     * @date 2022/10/4 16:52
     */
    @Transactional
    public void saveAliPayStatus(PayStatusDto payStatusDto){

        //支付记录号
        String payNO = payStatusDto.getOut_trade_no();
        XcPayRecord payRecordByPayno = getPayRecordByPayno(payNO);
        if(payRecordByPayno == null){
            XueChengPlusException.cast("找不到相关支付记录");
        }
        //拿到相关联订单id
        Long orderId = payRecordByPayno.getOrderId();
        XcOrders xcOrders = xcOrdersMapper.selectById(orderId);
        if(xcOrders == null){
            XueChengPlusException.cast("找不到相关订单");
        }
        //支付状态
        String statusFromDb = payRecordByPayno.getStatus();
        //如果数据库支付状态已经成功,不再处理
        if("601002".equals(statusFromDb)){
            //支付已经成功
            return;
        }
        //如果支付成功
        String tradeStatus = payStatusDto.getTrade_status();
        //返回信息为支付成功
        if("TRADE_SUCCESS".equals(tradeStatus)){
            //更新支付记录表支付状态为支付成功
            payRecordByPayno.setStatus("601002");
            //支付宝订单号
            payRecordByPayno.setOutPayNo(payStatusDto.getTrade_no());
            //第三方支付渠道编号
            payRecordByPayno.setOutPayChannel("Alipay");
            //支付成功时间
            payRecordByPayno.setPaySuccessTime(LocalDateTime.now());
            int update = xcPayRecordMapper.updateById(payRecordByPayno);
            if(update <= 0){
                XueChengPlusException.cast("更新支付记录失败");
            }
            //更新订单表支付状态为支付成功
            xcOrders.setStatus("600002");//订单已经支付成功

            int update1 = xcOrdersMapper.updateById(xcOrders);
            if(update1 <= 0){
                XueChengPlusException.cast("更新订单失败");
            }
            //保存消息记录,参数1：支付结果通知类型，2: 业务id，3:业务类型
            MqMessage payresultNotify = mqMessageService.addMessage("payresult_notify",
                    xcOrders.getOutBusinessId(),
                    xcOrders.getOrderType(),
                    null);
            //发送消息
            notifyPayResult(payresultNotify);
        }
    }

    @Override
    public void notifyPayResult(MqMessage message) {

        //消息内容
        String jsonString = JSON.toJSONString(message);

        Message messageObj = MessageBuilder.withBody(jsonString.getBytes(StandardCharsets.UTF_8))
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT).build();
        //全局消息ID
        Long id = message.getId();
        CorrelationData correlationData = new CorrelationData();
        //使用CorrelationData对象指定回调方法
        correlationData.getFuture().addCallback(
                result -> {
                    if(result.isAck()){
                        //消息成功发送到交换机
                        log.info("消息成功发送到交换机:{}",jsonString);
                        //将消息从数据库表删除
                        mqMessageService.completed(id);
                    }else{
                        //消息发送到交换机失败
                        log.info("消息发送到交换机失败:{}",jsonString);
                    }
                },
                ex -> {
                    //发生异常了
                    log.info("消息发送到交换机异常:{}",jsonString);
                }
        );
        rabbitTemplate.convertAndSend(PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT, "", messageObj, correlationData);
    }


    @Override
    public PayRecordDto queryPayResult(String payNo) throws AlipayApiException {
        //调用支付宝接口查询结果
        XcPayRecord payRecord = getPayRecordByPayno(payNo);
        if(payRecord == null){
            XueChengPlusException.cast("请重新点击获取二维码");
        }
        //根据支付流水号查询支付记录
        PayStatusDto payStatusDto = queryPayResultFromAlipay(payNo);
        System.out.println("payStatusDto = " + payStatusDto);
        if(payStatusDto == null){
            XueChengPlusException.cast("支付记录不存在");
        }

        //保存支付宝支付结果
        currentProxy.saveAliPayStatus(payStatusDto);

        //返回最新支付记录信息
        XcPayRecord payRecordByPayno = getPayRecordByPayno(payNo);
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecordByPayno, payRecordDto);
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
