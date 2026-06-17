package com.char1234.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.char1234.entity.Order;
import com.char1234.entity.OrderItem;
import com.char1234.entity.Product;
import com.char1234.entity.ProductReview;
import com.char1234.entity.UserAddress;
import com.char1234.entity.OrderRefund;
import com.char1234.mapper.OrderItemMapper;
import com.char1234.mapper.OrderMapper;
import com.char1234.mapper.OrderRefundMapper;
import com.char1234.mapper.ProductMapper;
import com.char1234.mapper.ProductReviewMapper;
import com.char1234.service.CartService;
import com.char1234.service.OrderService;
import com.char1234.service.ProductService;
import com.char1234.service.UserAddressService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.StringJoiner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 订单 Service 实现类
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserAddressService userAddressService;

    @Autowired
    private ProductReviewMapper productReviewMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderRefundMapper orderRefundMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long userId, List<Map<String, Object>> items,
                             Long addressId,
                             String receiverName, String receiverPhone, String receiverAddress) {
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("订单商品不能为空");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        List<Product> validated = new ArrayList<>();
        int[] qty = new int[items.size()];
        int i = 0;
        for (Map<String, Object> item : items) {
            Long productId = Long.valueOf(item.get("productId").toString());
            Integer quantity = Integer.valueOf(item.get("quantity").toString());
            qty[i] = quantity;
            Product product = productService.getById(productId);
            if (product == null) {
                throw new RuntimeException("商品不存在");
            }
            if (product.getStock() < quantity) {
                throw new RuntimeException("商品库存不足: " + product.getName());
            }
            validated.add(product);
            totalAmount = totalAmount.add(product.getPrice().multiply(new BigDecimal(quantity)));
            i++;
        }

        String rn = receiverName;
        String rp = receiverPhone;
        String ra = receiverAddress;
        Long aidSnapshot = null;
        if (addressId != null) {
            UserAddress ua = userAddressService.requireOwned(userId, addressId);
            aidSnapshot = ua.getId();
            rn = ua.getReceiverName();
            rp = ua.getPhone();
            ra = buildConcatAddress(ua);
        }
        if (StringUtils.isBlank(rn) || StringUtils.isBlank(rp) || StringUtils.isBlank(ra)) {
            throw new RuntimeException("请选择或填写完整收货信息");
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(0);
        order.setAddressId(aidSnapshot);
        order.setReceiverName(rn);
        order.setReceiverPhone(rp);
        order.setReceiverAddress(ra);
        order.setCreateTime(LocalDateTime.now());
        save(order);

        for (int k = 0; k < validated.size(); k++) {
            Product product = validated.get(k);
            int quantity = qty[k];

            OrderItem line = new OrderItem();
            line.setOrderId(order.getId());
            line.setProductId(product.getId());
            line.setProductName(product.getName());
            line.setProductImage(product.getMainImage());
            line.setPrice(product.getPrice());
            line.setQuantity(quantity);
            orderItemMapper.insert(line);

            product.setStock(product.getStock() - quantity);
            productService.updateById(product);
        }

        // 下单成功后清除购物车中已购商品（不影响订单创建）
        try {
            for (Map<String, Object> item : items) {
                Long productId = Long.valueOf(item.get("productId").toString());
                cartService.removeFromCart(userId, productId);
            }
        } catch (Exception ignored) {
            // 购物车清除失败不影响订单
        }

        return order;
    }

    @Override
    public Page<Order> pageList(Integer page, Integer size, String orderNo, Integer status, Long userId) {
        Page<Order> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(orderNo)) {
            wrapper.like(Order::getOrderNo, orderNo);
        }
        if (status != null) {
            // 筛选"已完成"时同时包含正常完成和售后完成的订单
            if (status == 3) {
                wrapper.in(Order::getStatus, 3, 4);
            } else {
                wrapper.eq(Order::getStatus, status);
            }
        }
        if (userId != null) {
            wrapper.eq(Order::getUserId, userId);
        }

        wrapper.orderByDesc(Order::getCreateTime);

        Page<Order> result = page(pageParam, wrapper);

        // 为每个订单填充 items 和售后状态
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            List<Long> orderIds = result.getRecords().stream()
                    .map(Order::getId)
                    .collect(java.util.stream.Collectors.toList());
            List<OrderItem> allItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));
            Map<Long, List<OrderItem>> itemsByOrderId = allItems.stream()
                    .collect(java.util.stream.Collectors.groupingBy(OrderItem::getOrderId));
            // 查询每个订单的售后记录
            List<OrderRefund> allRefunds = orderRefundMapper.selectList(
                    new LambdaQueryWrapper<OrderRefund>().in(OrderRefund::getOrderId, orderIds));
            Map<Long, List<OrderRefund>> refundsByOrderId = allRefunds.stream()
                    .collect(java.util.stream.Collectors.groupingBy(OrderRefund::getOrderId));
            for (Order order : result.getRecords()) {
                order.setItems(itemsByOrderId.getOrDefault(order.getId(), new ArrayList<>()));
                List<OrderRefund> refunds = refundsByOrderId.get(order.getId());
                if (refunds != null && !refunds.isEmpty()) {
                    boolean hasActive = refunds.stream().anyMatch(r ->
                            r.getStatus() == 0 || r.getStatus() == 1 || r.getStatus() == 2);
                    order.setRefundStatus(hasActive ? 1 : 2);
                } else {
                    order.setRefundStatus(0);
                }
            }
        }

        return result;
    }

    @Override
    public Order getOrderDetail(Long id) {
        Order order = orderMapper.selectOrderById(id);
        if (order != null) {
            List<OrderRefund> refunds = orderRefundMapper.findByOrderId(id);
            if (refunds != null && !refunds.isEmpty()) {
                boolean hasActive = refunds.stream().anyMatch(r ->
                        r.getStatus() == 0 || r.getStatus() == 1 || r.getStatus() == 2);
                order.setRefundStatus(hasActive ? 1 : 2);
            } else {
                order.setRefundStatus(0);
            }
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(Long id) {
        Order order = getById(id);
        if (order == null || order.getStatus() != 0) {
            return false;
        }
        order.setStatus(1);
        boolean ok = updateById(order);
        if (ok) {
            increaseSales(id);
        }
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmAlipayPayment(Long id, String payNo) {
        boolean ok = lambdaUpdate()
                .eq(Order::getId, id)
                .eq(Order::getStatus, 0)
                .set(Order::getStatus, 1)
                .set(Order::getPayNo, payNo)
                .set(Order::getPayTime, LocalDateTime.now())
                .update();
        if (ok) {
            increaseSales(id);
        }
        return ok;
    }

    /**
     * 异步通知到达后补充更新 payNo 和 payTime（与 confirmAlipayPayment 分开执行）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePayDetails(Long id, String payNo) {
        return lambdaUpdate()
                .eq(Order::getId, id)
                .eq(Order::getStatus, 1)
                .set(Order::getPayNo, payNo)
                .set(Order::getPayTime, LocalDateTime.now())
                .update();
    }

    /**
     * 更新商品销量（被 payOrder / confirmAlipayPayment 共用）
     */
    private void increaseSales(Long id) {
        List<OrderItem> lines = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        for (OrderItem line : lines) {
            Product p = productService.getById(line.getProductId());
            if (p != null) {
                int sc = p.getSalesCount() == null ? 0 : p.getSalesCount();
                p.setSalesCount(sc + line.getQuantity());
                productService.updateById(p);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean shipOrder(Long id, String company, String trackingNo) {
        Order order = getById(id);
        if (order == null || order.getStatus() != 1) {
            return false;
        }
        // 退款处理中的订单禁止发货（仅检查进行中的状态，已退款/已驳回/已撤销不阻塞）
        List<OrderRefund> refunds = orderRefundMapper.findByOrderId(id);
        boolean hasActiveRefund = refunds.stream().anyMatch(r ->
                r.getStatus() == 0 || r.getStatus() == 1 || r.getStatus() == 2 || r.getStatus() == 3);
        if (hasActiveRefund) {
            return false;
        }
        order.setStatus(2);
        order.setExpressCompany(company);
        order.setExpressNo(trackingNo);
        return updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long id) {
        Order order = getById(id);
        if (order == null) {
            return false;
        }
        if (order.getStatus() != 0) {
            return false;
        }
        List<OrderItem> lines = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        for (OrderItem line : lines) {
            Product p = productService.getById(line.getProductId());
            if (p != null) {
                p.setStock((p.getStock() == null ? 0 : p.getStock()) + line.getQuantity());
                productService.updateById(p);
            }
        }
        order.setStatus(-1);
        return updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmOrder(Long id) {
        Order order = getById(id);
        if (order == null || order.getStatus() != 2) {
            return false;
        }
        order.setStatus(3);
        return updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOrder(Long userId, Long id) {
        Order order = getById(id);
        if (order == null) {
            return false;
        }
        if (!order.getUserId().equals(userId)) {
            return false;
        }
        if (order.getStatus() != 3 && order.getStatus() != -1) {
            return false;
        }
        // 删除关联的商品评价，并更新商品评分
        List<ProductReview> reviews = productReviewMapper.selectList(
                new LambdaQueryWrapper<ProductReview>().eq(ProductReview::getOrderId, id));
        Set<Long> reviewProductIds = reviews.stream()
                .map(ProductReview::getProductId)
                .collect(Collectors.toSet());
        if (!reviews.isEmpty()) {
            productReviewMapper.delete(
                    new LambdaQueryWrapper<ProductReview>().eq(ProductReview::getOrderId, id));
        }
        // 删除订单项
        orderItemMapper.delete(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        // 删除订单
        removeById(id);
        // 重新计算受影响的商品评分
        for (Long pid : reviewProductIds) {
            updateProductRating(pid);
        }
        return true;
    }

    private void updateProductRating(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) return;

        List<ProductReview> all = productReviewMapper.selectList(
                new LambdaQueryWrapper<ProductReview>().eq(ProductReview::getProductId, productId));
        int count = all.size();
        if (count > 0) {
            int sum = all.stream().mapToInt(ProductReview::getRating).sum();
            BigDecimal avg = BigDecimal.valueOf(sum)
                    .divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP);
            product.setAvgRating(avg);
        } else {
            product.setAvgRating(BigDecimal.ZERO);
        }
        product.setReviewCount(count);
        productMapper.updateById(product);
    }

    @Override
    public List<Map<String, Object>> getSalesTrend(Integer days) {
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = start.plusDays(1);
            List<Order> dayOrders = list(new LambdaQueryWrapper<Order>()
                    .ge(Order::getCreateTime, start)
                    .lt(Order::getCreateTime, end)
                    .gt(Order::getStatus, 0));
            BigDecimal sales = dayOrders.stream()
                    .map(o -> o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("date", date.format(formatter));
            data.put("sales", sales);
            data.put("orders", dayOrders.size());
            result.add(data);
        }

        return result;
    }

    @Override
    public Map<String, Object> getOrderStatistics() {
        Map<String, Object> m = new HashMap<>();
        m.put("pending", count(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 0)));
        m.put("paid", count(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 1)));
        m.put("shipped", count(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 2)));
        m.put("completed", count(new LambdaQueryWrapper<Order>().in(Order::getStatus, 3, 4)));
        m.put("cancelled", count(new LambdaQueryWrapper<Order>().eq(Order::getStatus, -1)));
        m.put("totalOrders", count());
        return m;
    }

    private static String buildConcatAddress(UserAddress ua) {
        StringJoiner joiner = new StringJoiner(" ");
        if (StringUtils.isNotBlank(ua.getProvince())) {
            joiner.add(ua.getProvince().trim());
        }
        if (StringUtils.isNotBlank(ua.getCity())) {
            joiner.add(ua.getCity().trim());
        }
        if (StringUtils.isNotBlank(ua.getDistrict())) {
            joiner.add(ua.getDistrict().trim());
        }
        joiner.add(ua.getDetailAddress().trim());
        return joiner.toString();
    }

    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
