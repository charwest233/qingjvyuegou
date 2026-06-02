package com.char1234.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.char1234.entity.Order;

import java.util.List;
import java.util.Map;

/**
 * 订单 Service 接口
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单：可选用 addressId 自动填充收件信息，否则手写三项。
     */
    Order createOrder(Long userId, List<Map<String, Object>> items,
                      Long addressId,
                      String receiverName, String receiverPhone, String receiverAddress);

    /**
     * 分页查询订单列表
     */
    Page<Order> pageList(Integer page, Integer size, String orderNo, Integer status, Long userId);

    /**
     * 获取订单详情
     */
    Order getOrderDetail(Long id);

    /**
     * 支付订单(模拟支付)
     */
    boolean payOrder(Long id);

    /**
     * 发货
     */
    boolean shipOrder(Long id, String company, String trackingNo);

    /**
     * 取消订单（未支付回补库存）
     */
    boolean cancelOrder(Long id);

    /**
     * 确认收货（状态 2→3）
     */
    boolean confirmOrder(Long id);

    /**
     * 用户删除已完成的订单（status=3 或 -1）
     */
    boolean deleteOrder(Long userId, Long id);

    /**
     * 获取销售趋势数据
     */
    List<Map<String, Object>> getSalesTrend(Integer days);

    /**
     * 订单状态数量统计（管理端）
     */
    Map<String, Object> getOrderStatistics();
}
