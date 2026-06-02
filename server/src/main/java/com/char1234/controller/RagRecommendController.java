package com.char1234.controller;

import com.char1234.common.Result;
import com.char1234.service.ProductRagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * RAG 模糊推荐 Controller
 */
@RestController
@RequestMapping("/api/product")
public class RagRecommendController {

    @Autowired
    private ProductRagService productRagService;

    /**
     * RAG 语义搜索（自然语言模糊推荐）
     * GET /api/product/rag-search?q=便宜的办公笔记本&limit=8
     */
    @GetMapping("/rag-search")
    public Result<List<Map<String, Object>>> ragSearch(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "8") Integer limit) {
        if (query == null || query.isBlank()) {
            return Result.error("请输入搜索内容");
        }
        List<Map<String, Object>> results = productRagService.searchByQuery(query, limit);
        return Result.success(results);
    }
}
