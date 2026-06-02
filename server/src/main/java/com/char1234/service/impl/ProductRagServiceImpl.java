package com.char1234.service.impl;

import com.char1234.entity.Product;
import com.char1234.mapper.ProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * RAG 模糊推荐实现（基于关键词提取 + 数据库匹配）
 * 流程：自然语言查询 → 提取关键词/价格范围 → 数据库模糊匹配 → 评分排序
 */
@Slf4j
@Service
public class ProductRagServiceImpl implements com.char1234.service.ProductRagService {

    @Autowired
    private ProductMapper productMapper;

    // 停用词表（过滤无意义词语）
    private static final Set<String> STOP_WORDS = Set.of(
        "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一", "一个", "上", "也", "很",
        "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好", "自己", "这", "那", "这些", "那些",
        "适合", "想要", "需要", "推荐", "一款", "一些", "东西", "产品", "商品", "找", "买",
        "还有", "什么", "给", "或者", "吧", "吗", "啊", "呢", "呀", "哦", "嗯",
        "用", "可以", "比较", "哪个", "如何", "怎么", "哪款"
    );

    // 品类同义词映射
    private static final Map<String, List<String>> CATEGORY_SYNONYMS = Map.of(
        "手机", Arrays.asList("手机数码", "手机", "iPhone", "华为", "小米", "苹果", "安卓", "智能手机", "5G", "旗舰手机", "折叠屏"),
        "平板", Arrays.asList("平板", "iPad", "平板电脑", "pad", "tablet"),
        "电脑", Arrays.asList("电脑办公", "电脑", "笔记本", "笔记本电脑", "MacBook", "ThinkPad", "办公本", "游戏本", "轻薄本", "台式机", "PC"),
        "耳机", Arrays.asList("耳机", "蓝牙耳机", "无线耳机", "降噪耳机", "入耳式", "头戴式", "耳塞", "HiFi"),
        "显示器", Arrays.asList("显示器", "屏幕", "显示屏", "4K", "高刷", "27寸", "32寸"),
        "家电", Arrays.asList("家用电器", "家电", "空调", "冰箱", "洗衣机", "电视", "电饭煲", "微波炉", "烤箱", "净水器", "扫地机"),
        "美妆", Arrays.asList("美妆", "美妆护肤", "护肤", "彩妆", "口红", "唇膏", "粉底", "气垫", "眉笔", "眼影", "腮红", "化妆", "唇妆", "底妆", "面霜", "精华", "面膜", "护肤品"),
        "食品", Arrays.asList("食品饮料", "食品", "零食", "牛奶", "饼干", "饮料", "礼盒", "年货", "坚果", "巧克力"),
        "穿戴", Arrays.asList("手表", "智能手表", "手环", "手链", "腕表")
    );

    // 价格区间映射
    private static final List<Map.Entry<Pattern, BigDecimal[]>> PRICE_PATTERNS = List.of(
        Map.entry(Pattern.compile("便宜|实惠|低价|性价比|入门|经济"), new BigDecimal[]{BigDecimal.ZERO, new BigDecimal("300")}),
        Map.entry(Pattern.compile("两百|二百|200"), new BigDecimal[]{new BigDecimal("100"), new BigDecimal("300")}),
        Map.entry(Pattern.compile("五百|500"), new BigDecimal[]{new BigDecimal("300"), new BigDecimal("600")}),
        Map.entry(Pattern.compile("千元|一千|1000"), new BigDecimal[]{new BigDecimal("700"), new BigDecimal("1300")}),
        Map.entry(Pattern.compile("两千|2000|二千"), new BigDecimal[]{new BigDecimal("1500"), new BigDecimal("2500")}),
        Map.entry(Pattern.compile("三千|3000"), new BigDecimal[]{new BigDecimal("2500"), new BigDecimal("3500")}),
        Map.entry(Pattern.compile("四千|4000"), new BigDecimal[]{new BigDecimal("3500"), new BigDecimal("5000")}),
        Map.entry(Pattern.compile("五千|5000"), new BigDecimal[]{new BigDecimal("4000"), new BigDecimal("6000")}),
        Map.entry(Pattern.compile("高端|旗舰|豪华|顶配|奢侈"), new BigDecimal[]{new BigDecimal("5000"), new BigDecimal("99999")})
    );

    /**
     * 从查询中提取关键词
     */
    private List<String> extractKeywords(String query) {
        List<String> keywords = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        // 1. 提取中文词组（2-6字）
        java.util.regex.Matcher m = Pattern.compile("[\\u4e00-\\u9fa5]{2,6}").matcher(query);
        while (m.find()) {
            String word = m.group();
            if (!STOP_WORDS.contains(word)) {
                keywords.add(word);
            }
        }

        // 2. 提取英文/数字词
        m = Pattern.compile("[a-zA-Z]+[0-9]*|[0-9]+[a-zA-Z]*").matcher(query);
        while (m.find()) {
            keywords.add(m.group().toLowerCase());
        }

        // 3. 根据品类同义词扩展关键词
        for (Map.Entry<String, List<String>> entry : CATEGORY_SYNONYMS.entrySet()) {
            if (entry.getValue().stream().anyMatch(kw -> query.contains(kw))) {
                keywords.addAll(entry.getValue());
            }
        }

        return keywords.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 解析查询中的价格范围
     */
    private BigDecimal[] parsePriceRange(String query) {
        // 直接数字匹配（如 "3000元", "500块"）
        java.util.regex.Matcher m = Pattern.compile("(\\d{3,5})[元块]").matcher(query);
        if (m.find()) {
            BigDecimal target = new BigDecimal(m.group(1));
            return new BigDecimal[]{
                target.multiply(new BigDecimal("0.6")),
                target.multiply(new BigDecimal("1.4"))
            };
        }
        // 价格关键词匹配
        for (Map.Entry<Pattern, BigDecimal[]> entry : PRICE_PATTERNS) {
            if (entry.getKey().matcher(query).find()) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> searchByQuery(String query, int limit) {
        if (query == null || query.isBlank()) {
            return toResultList(productMapper.selectHotProducts(limit));
        }

        // Step 1: 提取关键词和价格范围
        String lowerQuery = query.toLowerCase();
        List<String> keywords = extractKeywords(lowerQuery);
        BigDecimal[] priceRange = parsePriceRange(lowerQuery);

        log.info("RAG搜索: query={}, keywords={}, priceRange={}",
            query, keywords, priceRange != null ? priceRange[0] + "~" + priceRange[1] : "无");

        // Step 2: 从数据库获取候选商品（多维度搜索）
        List<Product> candidates = searchCandidates(keywords, priceRange);

        // Step 3: 评分排序
        List<Map<String, Object>> results = candidates.stream()
            .map(p -> computeScore(p, keywords, lowerQuery, priceRange))
            .sorted((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")))
            .limit(limit)
            .collect(Collectors.toList());

        // Step 4: 检查是否有有效的关键词匹配结果
        boolean hasKeywordMatch = results.stream().anyMatch(item -> (Double) item.get("score") > 0);

        // Step 5: 无关键词匹配时，用热门商品兜底并标记
        if (!hasKeywordMatch) {
            return toResultList(productMapper.selectHotProducts(limit));
        }

        return results;
    }

    /**
     * 从数据库搜索候选商品
     */
    private List<Product> searchCandidates(List<String> keywords, BigDecimal[] priceRange) {
        Set<Long> candidateIds = new LinkedHashSet<>();

        // 1. 关键词搜索
        for (String kw : keywords) {
            if (kw.length() < 2) continue;
            List<Product> matches = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                    .like(Product::getName, kw)
                    .or()
                    .like(Product::getDescription, kw)
                    .last("LIMIT 20")
            );
            matches.forEach(p -> candidateIds.add(p.getId()));
        }

        // 2. 价格区间搜索
        if (priceRange != null) {
            List<Product> priceMatches = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                    .ge(Product::getPrice, priceRange[0])
                    .le(Product::getPrice, priceRange[1])
                    .orderByDesc(Product::getSalesCount)
                    .last("LIMIT 20")
            );
            priceMatches.forEach(p -> candidateIds.add(p.getId()));
        }

        // 3. 候选商品数量不足时，补充热门商品
        if (candidateIds.size() < 5) {
            List<Product> hot = productMapper.selectHotProducts(15);
            hot.forEach(p -> candidateIds.add(p.getId()));
        }

        if (candidateIds.isEmpty()) {
            return productMapper.selectHotProducts(15);
        }

        // 批量查询
        return productMapper.selectList(
            new LambdaQueryWrapper<Product>().in(Product::getId, candidateIds)
        );
    }

    /**
     * 综合评分
     */
    private Map<String, Object> computeScore(Product p, List<String> keywords,
                                              String query, BigDecimal[] priceRange) {
        double score = 0;
        String name = p.getName() != null ? p.getName().toLowerCase() : "";
        String desc = p.getDescription() != null ? p.getDescription().toLowerCase() : "";
        BigDecimal price = p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO;

        // 名称精确匹配（高权重）
        if (name.contains(query)) score += 20;
        if (desc.contains(query)) score += 10;

        // 关键词逐项匹配
        for (String kw : keywords) {
            String kwL = kw.toLowerCase();
            if (name.contains(kwL)) {
                score += 12;
            } else if (name.contains(kwL.substring(0, Math.min(2, kwL.length())))) {
                score += 6;
            }
            if (desc.contains(kwL)) {
                score += 4;
            }
        }

        // 价格匹配加分
        if (priceRange != null) {
            if (price.compareTo(priceRange[0]) >= 0 && price.compareTo(priceRange[1]) <= 0) {
                score += 8;
            }
        }

        // 销量和评分的正向激励
        if (p.getSalesCount() != null && p.getSalesCount() > 0) {
            score += Math.min(p.getSalesCount() * 0.1, 5);
        }
        if (p.getAvgRating() != null) {
            score += p.getAvgRating().doubleValue() * 1.5;
        }

        // 构造结果
        Map<String, Object> item = new HashMap<>();
        item.put("id", p.getId());
        item.put("categoryId", p.getCategoryId());
        item.put("name", p.getName());
        item.put("price", p.getPrice());
        item.put("stock", p.getStock());
        item.put("mainImage", p.getMainImage());
        item.put("description", p.getDescription());
        item.put("salesCount", p.getSalesCount());
        item.put("avgRating", p.getAvgRating());
        item.put("reviewCount", p.getReviewCount());
        item.put("score", Math.round(score * 10) / 10.0);
        item.put("similarity", Math.min(Math.max(score / 40, 0), 1));

        return item;
    }

    /**
     * 将 Product 列表转为 Map 结果
     */
    private List<Map<String, Object>> toResultList(List<Product> products) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (Product p : products) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("categoryId", p.getCategoryId());
            item.put("name", p.getName());
            item.put("price", p.getPrice());
            item.put("stock", p.getStock());
            item.put("mainImage", p.getMainImage());
            item.put("description", p.getDescription());
            item.put("salesCount", p.getSalesCount());
            item.put("avgRating", p.getAvgRating());
            item.put("reviewCount", p.getReviewCount());
            item.put("score", 0.0);
            item.put("similarity", 0.0);
            item.put("isFallback", true);
            results.add(item);
        }
        return results;
    }
}
