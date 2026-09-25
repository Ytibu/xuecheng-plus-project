package com.xuecheng.orders.service;

import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcPayRecord;

public interface OrderService {

    /**
     * 创建商品订单
     * @param userId 用户ID
     * @param addOrderDto 订单信息
     * @return 支付记录（包含二维码）
     */
    PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);


    /**
     * 请求支付宝查询支付结果
     * @param payNo 支付记录id
     * @return 支付记录信息
     */
    PayRecordDto queryPayResult(String payNo);


    /**
     * 保存订单信息到订单表中
     * @param userId 用户ID
     * @param addOrderDto 订单信息
     * @return 订单
     */
    XcOrders saveXcOrders(String userId, AddOrderDto addOrderDto);


    /**
     * 查询支付记录
     * @param payNo 订单支付号
     * @return 支付记录
     */
    XcPayRecord getPayRecordByPayNo(String payNo);


    void saveAliPayStatus(PayStatusDto payStatusDto);
}