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
import com.xuecheng.orders.config.AlipayConfig;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private XcOrdersMapper xcOrdersMapper;
    @Autowired
    XcOrdersGoodsMapper ordersGoodsMapper;
    @Autowired
    XcPayRecordMapper payRecordMapper;

    @Autowired
    private OrderService currentProxy;

    @Value("${pay.qrcodeurl}")
    String qrcodeUrl;

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;

    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto)
    {
        // 插入订单表
        XcOrders xcOrders = saveXcOrders(userId, addOrderDto);

        // 插入支付记录
        XcPayRecord payRecord = createPayRecord(xcOrders);
        Long payNo = payRecord.getPayNo();

        // 二维码生成
        QRCodeUtil qrCodeUtil = new QRCodeUtil();
        String url = String.format(qrcodeUrl, payNo);

        String qrCode = null;
        try {
            qrCode = qrCodeUtil.createQRCode(url, 200, 200);
        } catch (IOException e) {
            XueChengPlusException.cast("二维码生成错误");
        }

        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);
        payRecordDto.setQrcode(qrCode);

        return payRecordDto;
    }

    /**
     * 保存支付记录
     * @param orders 订单信息
     * @return 订单记录
     */
    public XcPayRecord createPayRecord(XcOrders orders)
    {
        Long ordersId = orders.getId();
        XcOrders xcOrders = xcOrdersMapper.selectById(ordersId);
        if(xcOrders == null){
            XueChengPlusException.cast("订单不存在");
        }

        String status = xcOrders.getStatus();
        if("601002".equals(status)){
            XueChengPlusException.cast("此订单已支付");
        }

        XcPayRecord xcPayRecord = new XcPayRecord();
        xcPayRecord.setPayNo(IdWorkerUtils.getInstance().nextId());
        xcPayRecord.setOrderId(orders.getId());
        xcPayRecord.setOrderName(orders.getOrderName());
        xcPayRecord.setTotalPrice(orders.getTotalPrice());
        xcPayRecord.setCurrency("CNY");
        xcPayRecord.setCreateDate(LocalDateTime.now());
        xcPayRecord.setStatus("601001");
        xcPayRecord.setUserId(xcOrders.getUserId());
        int insert = payRecordMapper.insert(xcPayRecord);
        if(insert <= 0){
            XueChengPlusException.cast("支付记录保存失败");
        }

        return xcPayRecord;
    }

    /**
     * 保存订单信息
     * @param userId 用户ID
     * @param addOrderDto 商品订单（orderDetail字段保存了所有商品的信息）
     * @return 该笔订单
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public XcOrders saveXcOrders(String userId, AddOrderDto addOrderDto){
        //幂等性处理（先确认该订单真的不存在）
        XcOrders order = xcOrdersMapper.selectOne(new LambdaQueryWrapper<XcOrders>()
                .eq(XcOrders::getOutBusinessId, addOrderDto.getOutBusinessId()));
        if(order != null){
            return order;
        }

        // 创建订单并保存进订单表
        order = new XcOrders();
        //生成订单号
        long orderId = IdWorkerUtils.getInstance().nextId();
        order.setId(orderId);
        order.setTotalPrice(addOrderDto.getTotalPrice());
        order.setCreateDate(LocalDateTime.now());
        order.setStatus("600001");//未支付
        order.setUserId(userId);
        order.setOrderType(addOrderDto.getOrderType());
        order.setOrderName(addOrderDto.getOrderName());
        order.setOrderDetail(addOrderDto.getOrderDetail());
        order.setOrderDescrip(addOrderDto.getOrderDescrip());
        order.setOutBusinessId(addOrderDto.getOutBusinessId());//选课记录id
        int insert = xcOrdersMapper.insert(order);
        if(insert <= 0){
            XueChengPlusException.cast("添加订单失败");
        }

        // 将订单商品信息保存进订单商品表
        String orderDetailJson = addOrderDto.getOrderDetail();
        List<XcOrdersGoods> xcOrdersGoodsList = JSON.parseArray(orderDetailJson, XcOrdersGoods.class);
        xcOrdersGoodsList.forEach(goods->{
            XcOrdersGoods xcOrdersGoods = new XcOrdersGoods();
            BeanUtils.copyProperties(goods,xcOrdersGoods);
            xcOrdersGoods.setOrderId(orderId);//订单号
            ordersGoodsMapper.insert(xcOrdersGoods);
        });
        return order;
    }

    @Override
    public XcPayRecord getPayRecordByPayNo(String payNo)
    {
        return payRecordMapper.selectOne(new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
    }

    @Override
    public PayRecordDto queryPayResult(String payNo){

        //从支付宝查询支付结果
        PayStatusDto payStatusDto = queryPayResultFromAlipay(payNo);
        //保存支付结果
        currentProxy.saveAliPayStatus(payStatusDto);

        XcPayRecord payRecord = getPayRecordByPayNo(payNo);
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);

        return payRecordDto;
    }

    /**
     * 请求支付宝查询支付结果
     * @param payNo 支付交易号
     * @return 支付结果
     */
    public PayStatusDto queryPayResultFromAlipay(String payNo){

        //========请求支付宝查询支付结果=============
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE); //获得初始化的AlipayClient
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payNo);
        request.setBizContent(bizContent.toString());
        String body = null;
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            body = response.getBody();
        } catch (AlipayApiException e) {
            log.error("请求支付宝查询支付结果异常:{}", e.toString(), e);
            XueChengPlusException.cast("请求支付查询查询失败");
        }

        //获取支付结果
        //转map
        Map resultMap = JSON.parseObject(body, Map.class);
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
     * 保存支付宝支付结果
     * @param payStatusDto  支付结果信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveAliPayStatus(PayStatusDto payStatusDto)
    {
        String outTradeNo = payStatusDto.getOut_trade_no();
        XcPayRecord record = getPayRecordByPayNo(outTradeNo);
        if (record == null) {
            XueChengPlusException.cast("支付记录不存在");
        }

        Long orderId = record.getOrderId();
        XcOrders xcOrders = xcOrdersMapper.selectById(orderId);
        if(xcOrders==null){
            XueChengPlusException.cast("关联订单不存在");
        }

        String statusFromDB = record.getStatus();
        if("601002".equals(statusFromDB)) {
            return;
        }

        String tradeStatus = payStatusDto.getTrade_status();
        if("TRADE_SUCCESS".equals(tradeStatus))
        {
            record.setStatus("601002");
            record.setOutPayNo(payStatusDto.getOut_trade_no());
            record.setOutPayChannel("Alipay");
            record.setPaySuccessTime(LocalDateTime.now());
            payRecordMapper.updateById(record);

            xcOrders.setStatus("600002");
            xcOrdersMapper.updateById(xcOrders);
        }
    }

}
