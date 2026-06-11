package com.char1234.ai;

import com.char1234.entity.CartItem;
import com.char1234.service.CartService;
import com.char1234.service.ProductRagService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 智能客服的工具类，提供购物车操作能力。
 * 通过 @Tool 注解暴露给 LangChain4j，LLM 可自动调用。
 */
@Slf4j
@Component
public class CartTool {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRagService productRagService;

    /**
     * 搜索商品：根据名称关键词搜索，返回商品列表信息
     */
    @Tool("根据商品名称搜索商品，返回商品ID、名称、价格等信息。调用前先通过此工具获取商品ID")
    public String searchProduct(@P("搜索关键词，如'草莓'、'耳机'、'运动鞋'") String keyword) {
        log.info("AI搜索商品: keyword={}", keyword);
        List<Map<String, Object>> products = productRagService.searchByQuery(keyword, 5);
        if (products.isEmpty()) {
            return "未找到与【" + keyword + "】相关的商品";
        }
        return products.stream()
                .map(p -> "ID=" + p.get("id") +
                        " 名称=" + p.get("name") +
                        " 价格=" + p.get("price") + "元" +
                        " 库存=" + p.get("stock"))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 添加商品到购物车
     */
    @Tool("添加商品到购物车。注意：需先通过 searchProduct 搜索获取商品ID后再调用此方法")
    public String addCart(
            @ToolMemoryId String userId,
            @P("商品ID，通过 searchProduct 获取") Long productId,
            @P("购买数量，默认为1") Integer quantity) {
        log.info("AI加购物车: userId={}, productId={}, quantity={}", userId, productId, quantity);
        cartService.addToCart(Long.valueOf(userId), productId, quantity != null ? quantity : 1);
        return "已成功添加商品(ID=" + productId + ")到购物车" +
                (quantity != null && quantity > 1 ? "，数量" + quantity + "件" : "");
    }

    /**
     * 查询购物车内容
     */
    @Tool("查询当前用户的购物车内容，返回商品名称、数量、价格等信息")
    public String selectCart(@ToolMemoryId String userId) {
        log.info("AI查购物车: userId={}", userId);
        List<CartItem> items = cartService.getCartList(Long.valueOf(userId));
        if (items == null || items.isEmpty()) {
            return "您的购物车是空的，快去挑选心仪的商品吧！";
        }
        StringBuilder sb = new StringBuilder("您的购物车有 " + items.size() + " 件商品：\n");
        double total = 0;
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            double subtotal = item.getPrice() * item.getQuantity();
            total += subtotal;
            sb.append((i + 1) + ". ").append(item.getProductName())
                    .append(" × ").append(item.getQuantity())
                    .append(" = ").append(String.format("%.2f", subtotal))
                    .append("元");
            if (item.isSelected()) {
                sb.append("（已选中）");
            }
            sb.append("\n");
        }
        sb.append("总计：").append(String.format("%.2f", total)).append("元");
        return sb.toString();
    }

    /**
     * 从购物车移除商品
     */
    @Tool("从购物车移除指定商品")
    public String removeFromCart(
            @ToolMemoryId String userId,
            @P("要移除的商品名称或商品ID") String productNameOrId) {
        log.info("AI移除购物车商品: userId={}, productNameOrId={}", userId, productNameOrId);
        Long productId;
        try {
            productId = Long.parseLong(productNameOrId);
        } catch (NumberFormatException e) {
            List<Map<String, Object>> products = productRagService.searchByQuery(productNameOrId, 1);
            if (products.isEmpty()) {
                return "未找到商品【" + productNameOrId + "】";
            }
            productId = ((Number) products.get(0).get("id")).longValue();
        }
        cartService.removeFromCart(Long.valueOf(userId), productId);
        return "已从购物车移除商品(ID=" + productId + ")";
    }

    /**
     * 清空购物车
     */
    @Tool("清空当前用户的购物车")
    public String clearCart(@ToolMemoryId String userId) {
        log.info("AI清空购物车: userId={}", userId);
        cartService.clearCart(Long.valueOf(userId));
        return "购物车已清空";
    }
}
