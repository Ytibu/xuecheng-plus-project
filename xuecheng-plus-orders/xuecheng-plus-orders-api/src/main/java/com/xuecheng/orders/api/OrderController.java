package com.xuecheng.orders.api;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.orders.config.AlipayConfig;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import com.xuecheng.orders.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@Api(value = "订单支付接口", tags = "订单支付接口")
@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Value("${pay.alipay.APP_ID}")
    String APP_ID;

    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

    @ApiOperation("生成支付二维码")
    @PostMapping("/generatepaycode")
    @ResponseBody
    public PayRecordDto generatePayCode(@RequestBody AddOrderDto addOrderDto)
    {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user == null){
            XueChengPlusException.cast("账号信息不明确");
        }
        String userId = user.getId();

        return orderService.createOrder(userId, addOrderDto);
    }

    @ApiOperation("扫码下单接口")
    @GetMapping("/requestpay")
    public void requestpay(String payNo, HttpServletResponse httpResponse) throws Exception {
        XcPayRecord record = orderService.getPayRecordByPayNo(payNo);
        if(record == null){
            XueChengPlusException.cast("未支付");
        }
        String status = record.getStatus();
        if("601002".equals(status)){
            XueChengPlusException.cast("无需重复支付");
        }

        AlipayClient alipayClient = new DefaultAlipayClient(
                AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY,AlipayConfig.SIGNTYPE);

        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
        // alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
         alipayRequest.setNotifyUrl("http://762976b8.r9.cpolar.cn/api/orders/paynotify");//在公共参数中设置回跳和通知地址
        alipayRequest.setBizContent("{" +
                "    \"out_trade_no\":\"" + payNo + "\"," +
                "    \"total_amount\":" + record.getTotalPrice() + "," +
                "    \"subject\":\"" + record.getOrderName() +"\"," +
                "    \"product_code\":\"QUICK_WAP_WAY\"" +
                "  }");//填充业务参数
        String form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        httpResponse.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
    }

    @ApiOperation("查询支付结果")
    @GetMapping("/payresult")
    @ResponseBody
    public PayRecordDto payresult(String payNo)
    {
        return orderService.queryPayResult(payNo);
    }

    @PostMapping("/paynotify")
    public void paynotify(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Exception {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> parameterMap = httpRequest.getParameterMap();
        for (String name : parameterMap.keySet()) {
            String[] values = (String[]) parameterMap.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        boolean verify = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, AlipayConfig.CHARSET, "RSA2");
        if(verify){
            String outTradeNo = new String(httpRequest.getParameter("out_trade_no").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            String tradeNo = new String(httpRequest.getParameter("trade_no").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            String tradeStatus = new String(httpRequest.getParameter("trade_status").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            String totalMount = new String(httpRequest.getParameter("total_mount").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            if (tradeStatus.equals("TRADE_SUCCESS")) {
                System.out.println(tradeStatus);
                PayStatusDto payStatusDto = new PayStatusDto();
                payStatusDto.setTrade_status(tradeStatus);
                payStatusDto.setTrade_no(tradeNo);
                payStatusDto.setOut_trade_no(outTradeNo);
                payStatusDto.setTotal_amount(totalMount);
                payStatusDto.setApp_id(APP_ID);
                orderService.saveAliPayStatus(payStatusDto);
            }

            httpResponse.getWriter().write("success");
        }else{
            httpResponse.getWriter().write("fail");
        }
    }

}