package com.char1234.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.char1234.config.AlipayProperties;
import com.char1234.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付宝支付核心服务
 */
@Slf4j
@Service
public class AlipayService {

    @Autowired
    private AlipayProperties alipayProperties;

    @Autowired
    private OrderService orderService;

    private AlipayClient alipayClient;

    /**
     * 获取（懒加载）支付宝客户端
     */
    private AlipayClient getAlipayClient() {
        if (alipayClient == null) {
            alipayClient = new DefaultAlipayClient(
                    alipayProperties.getGatewayUrl(),
                    alipayProperties.getAppId(),
                    alipayProperties.getAppPrivateKey(),
                    alipayProperties.getFormat(),
                    alipayProperties.getCharset(),
                    alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getSignType()
            );
        }
        return alipayClient;
    }

    /**
     * 创建支付宝支付，返回跳转表单 HTML
     */
    public String createTrade(Order order) {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayProperties.getNotifyUrl());
        request.setReturnUrl(alipayProperties.getReturnUrl());

        // 业务参数
        String bizContent = String.format(
                "{\"out_trade_no\":\"%s\",\"total_amount\":\"%s\",\"subject\":\"%s\",\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}",
                order.getOrderNo(),
                order.getTotalAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toString(),
                "悦选商城 - 订单 " + order.getOrderNo()
        );
        request.setBizContent(bizContent);

        try {
            return getAlipayClient().pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            log.error("创建支付宝支付失败 orderNo={}", order.getOrderNo(), e);
            throw new RuntimeException("创建支付宝支付失败: " + e.getErrMsg());
        }
    }

    /**
     * 验签并处理异步通知
     *
     * @param params 支付宝异步通知的全部参数
     * @return "success" 或 "failure"
     */
    public String handleNotify(Map<String, String> params) {
        try {
            // 1. 验签
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(),
                    alipayProperties.getSignType()
            );
            if (!signVerified) {
                log.warn("支付宝异步通知验签失败");
                return "failure";
            }

            // 2. 获取业务参数
            String outTradeNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");       // 支付宝交易号
            String tradeStatus = params.get("trade_status");

            log.info("支付宝异步通知: outTradeNo={}, tradeNo={}, status={}", outTradeNo, tradeNo, tradeStatus);

            // 3. 只处理交易完成状态
            if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
                return "success"; // 其他状态不处理但返回 success 避免支付宝重复通知
            }

            // 4. 根据 out_trade_no 查找订单并确认支付
            Order order = orderService.lambdaQuery()
                    .eq(Order::getOrderNo, outTradeNo)
                    .one();
            if (order == null) {
                log.warn("异步通知: 订单不存在 outTradeNo={}", outTradeNo);
                return "failure";
            }

            // 5. 幂等处理：已支付直接返回成功
            if (order.getStatus() != 0) {
                log.info("异步通知: 订单已处理 orderId={}, status={}", order.getId(), order.getStatus());
                return "success";
            }

            // 6. 确认支付（只更新 status）
            boolean ok = orderService.confirmAlipayPayment(order.getId(), tradeNo);
            if (ok) {
                // 7. 补充更新 payNo/payTime（异步通知确保字段存在）
                orderService.updatePayDetails(order.getId(), tradeNo);
                log.info("支付宝支付成功 orderId={}, tradeNo={}", order.getId(), tradeNo);
                return "success";
            } else {
                log.error("支付宝支付确认失败 orderId={}", order.getId());
                return "failure";
            }
        } catch (AlipayApiException e) {
            log.error("支付宝异步通知处理异常", e);
            return "failure";
        }
    }
}
