package com.char1234.controller;

import com.char1234.common.Result;
import com.char1234.config.AlipayProperties;
import com.char1234.entity.Order;
import com.char1234.service.AlipayService;
import com.char1234.service.OrderService;
import com.alipay.api.internal.util.AlipaySignature;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j

/**
 * 支付宝通知/回调 Controller
 */
@RestController
@RequestMapping("/api/alipay")
public class AlipayNotifyController {

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AlipayProperties alipayProperties;

    /**
     * 支付宝异步通知（POST，返回纯文本 success / failure）
     */
    @PostMapping(value = "/notify", produces = "text/plain;charset=UTF-8")
    public String notify(HttpServletRequest request) {
        Map<String, String> params = getParamsFromRequest(request);
        return alipayService.handleNotify(params);
    }

    /**
     * 支付宝同步跳转（支付成功后浏览器重定向到前端）
     */
    @GetMapping("/return")
    public void returnUrl(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> params = getParamsFromRequest(request);
        String frontendUrl = alipayProperties.getFrontendUrl();
        String redirectUrl;

        try {
            // 验签
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(),
                    alipayProperties.getSignType()
            );

            if (!signVerified) {
                log.warn("同步跳转验签失败");
                response.sendRedirect(frontendUrl + "/orders");
                return;
            }

            // 获取支付宝返回的参数（同步跳转不传 trade_status）
            String outTradeNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");

            log.info("同步跳转: outTradeNo={}, tradeNo={}", outTradeNo, tradeNo);

            if (outTradeNo == null || tradeNo == null) {
                log.warn("同步跳转: 缺少必要参数");
                response.sendRedirect(frontendUrl + "/orders");
                return;
            }

            // 根据 out_trade_no 查找订单
            Order order = orderService.lambdaQuery()
                    .eq(Order::getOrderNo, outTradeNo)
                    .one();

            if (order == null) {
                log.warn("同步跳转: 订单不存在 outTradeNo={}", outTradeNo);
                response.sendRedirect(frontendUrl + "/orders");
                return;
            }

            // 验签通过即表示支付成功，直接确认支付（同步跳转不传 trade_status）
            if (order.getStatus() == 0) {
                boolean ok = orderService.confirmAlipayPayment(order.getId(), tradeNo);
                log.info("同步跳转: 确认支付结果={}, orderId={}, tradeNo={}", ok, order.getId(), tradeNo);
                // 重新查询订单获取最新状态
                order = orderService.getById(order.getId());
            }

            // 跳转订单页（支付宝回跳后直接落到用户端订单列表）
            if (order.getStatus() >= 1) {
                redirectUrl = frontendUrl + "/orders?id=" + order.getId();
            } else {
                redirectUrl = frontendUrl + "/orders";
            }
        } catch (Exception e) {
            log.error("同步跳转处理异常", e);
            redirectUrl = frontendUrl + "/orders";
        }

        response.sendRedirect(redirectUrl);
    }

    /**
     * 从 HttpServletRequest 提取全部参数
     */
    private Map<String, String> getParamsFromRequest(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
            String[] values = entry.getValue();
            params.put(entry.getKey(), (values != null && values.length > 0) ? values[0] : "");
        }
        return params;
    }
}
