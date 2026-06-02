package com.char1234.service;

import com.char1234.entity.Product;

import java.util.List;
import java.util.Map;

/**
 * RAG 模糊推荐服务
 */
public interface ProductRagService {

    /**
     * RAG 语义搜索（自然语言模糊推荐）
     * @param query 用户自然语言查询
     * @param limit 返回数量
     * @return 商品列表（每个商品附加 similarity 相似度）
     */
    List<Map<String, Object>> searchByQuery(String query, int limit);
}
