package com.char1234.entity;

import lombok.Data;

/**
 * 购物车项实体（Redis Hash 存储）
 */
@Data
public class CartItem {
    private Long productId;
    private String productName;
    private String productImage;
    private Double price;
    private Integer quantity;
    private Integer selected; // 1-选中, 0-未选中
    private Long createTime;

    public boolean isSelected() {
        return selected != null && selected == 1;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected ? 1 : 0;
    }
}
