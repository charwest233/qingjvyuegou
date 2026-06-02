package com.char1234.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.char1234.common.Result;
import com.char1234.entity.Order;
import com.char1234.entity.Product;
import com.char1234.entity.User;
import com.char1234.service.OrderService;
import com.char1234.service.ProductService;
import com.char1234.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘 Controller
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        LocalDateTime yesterdayStart = yesterday.atStartOfDay();
        LocalDateTime yesterdayEnd = yesterdayStart.plusDays(1);

        // 用户统计
        Map<String, Object> userStats = userService.getStatistics();
        Number totalUsers = (Number) userStats.getOrDefault("total", 0);
        Number todayNewUsers = (Number) userStats.getOrDefault("todayNew", 0);
        long yesterdayNewUsers = userService.count(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, yesterdayStart)
                .lt(User::getCreateTime, yesterdayEnd));
        stats.put("totalUsers", totalUsers.longValue());
        stats.put("newUsers", todayNewUsers.longValue());

        // 商品统计
        long productCount = productService.count();
        LocalDateTime thisMonthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime lastMonthStart = thisMonthStart.minusMonths(1);
        long thisMonthProducts = productService.count(new LambdaQueryWrapper<Product>()
                .ge(Product::getCreateTime, thisMonthStart));
        long lastMonthProducts = productService.count(new LambdaQueryWrapper<Product>()
                .ge(Product::getCreateTime, lastMonthStart)
                .lt(Product::getCreateTime, thisMonthStart));
        stats.put("totalProducts", productCount);

        // 订单统计（真实数据）
        long todayOrders = orderService.count(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, todayStart)
                .lt(Order::getCreateTime, todayEnd));
        long yesterdayOrders = orderService.count(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, yesterdayStart)
                .lt(Order::getCreateTime, yesterdayEnd));
        BigDecimal todaySales = sumOrderAmount(orderService.list(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, todayStart)
                .lt(Order::getCreateTime, todayEnd)
                .gt(Order::getStatus, 0)));
        BigDecimal yesterdaySales = sumOrderAmount(orderService.list(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, yesterdayStart)
                .lt(Order::getCreateTime, yesterdayEnd)
                .gt(Order::getStatus, 0)));
        Map<String, Object> orderStats = orderService.getOrderStatistics();

        stats.put("todayOrders", todayOrders);
        stats.put("todaySales", todaySales);
        stats.put("orderTrend", calcTrend(todayOrders, yesterdayOrders));
        stats.put("salesTrend", calcTrend(todaySales, yesterdaySales));
        stats.put("userTrend", calcTrend(todayNewUsers.longValue(), yesterdayNewUsers));
        stats.put("productTrend", calcTrend(thisMonthProducts, lastMonthProducts));
        stats.put("orderStats", orderStats);

        return Result.success(stats);
    }

    /**
     * 获取销售趋势
     */
    @GetMapping("/sales-trend")
    public Result<Map<String, Object>> salesTrend(@RequestParam(defaultValue = "7") Integer days) {
        List<Map<String, Object>> trend = orderService.getSalesTrend(days);

        // 处理返回格式
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new java.util.ArrayList<>();
        List<BigDecimal> sales = new java.util.ArrayList<>();
        List<Integer> orders = new java.util.ArrayList<>();

        for (Map<String, Object> item : trend) {
            dates.add((String) item.get("date"));
            sales.add((BigDecimal) item.get("sales"));
            orders.add((Integer) item.get("orders"));
        }

        result.put("dates", dates);
        result.put("sales", sales);
        result.put("orders", orders);

        return Result.success(result);
    }

    /**
     * 获取热门商品
     */
    @GetMapping("/hot-products")
    public Result<List<Product>> hotProducts(@RequestParam(defaultValue = "10") Integer limit) {
        List<Product> list = productService.getHotProducts(limit);
        return Result.success(list);
    }

    /**
     * 获取最新订单
     */
    @GetMapping("/recent-orders")
    public Result<List<Map<String, Object>>> recentOrders(@RequestParam(defaultValue = "10") Integer limit) {
        Page<Order> page = orderService.pageList(1, limit, null, null, null);
        List<Map<String, Object>> list = new java.util.ArrayList<>();
        for (Order item : page.getRecords()) {
            Map<String, Object> order = new HashMap<>();
            order.put("orderNo", item.getOrderNo());
            order.put("totalAmount", item.getTotalAmount());
            order.put("status", item.getStatus());
            order.put("createTime", item.getCreateTime());
            list.add(order);
        }
        return Result.success(list);
    }

    private static BigDecimal sumOrderAmount(List<Order> orders) {
        return orders.stream()
                .map(o -> o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static double calcTrend(long current, long previous) {
        if (previous <= 0) {
            return current > 0 ? 100D : 0D;
        }
        return BigDecimal.valueOf((current - previous) * 100D / previous)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static double calcTrend(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) <= 0) {
            return (current != null && current.compareTo(BigDecimal.ZERO) > 0) ? 100D : 0D;
        }
        BigDecimal safeCurrent = current == null ? BigDecimal.ZERO : current;
        return safeCurrent.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
