package com.char1234.ai;

import com.char1234.service.ProductRagService;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ContentRetriever 适配器：包装现有的 ProductRagService（关键词搜索），
 * 为 LLM 提供商品知识库查询能力。
 */
@Component
public class ProductRagContentRetriever implements ContentRetriever {

    @Autowired
    private ProductRagService productRagService;

    @Override
    public List<Content> retrieve(Query query) {
        String text = query.text();
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<Map<String, Object>> products = productRagService.searchByQuery(text, 5);

        return products.stream()
                .map(p -> Content.from(
                        "商品ID: " + p.get("id") +
                        "，名称: " + p.get("name") +
                        "，价格: " + p.get("price") + "元" +
                        "，库存: " + p.get("stock") +
                        (p.get("description") != null ? "，描述: " + p.get("description") : "")
                ))
                .collect(Collectors.toList());
    }
}
