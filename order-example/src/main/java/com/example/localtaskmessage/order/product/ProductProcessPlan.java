package com.example.localtaskmessage.order.product;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record ProductProcessPlan(
        long rootProductId,
        List<Item> items
) {

    public ProductProcessPlan {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("商品处理计划至少包含根商品");
        }

        items = List.copyOf(items);

        if (items.get(items.size() - 1).productId() != rootProductId) {
            throw new IllegalArgumentException("商品处理计划最后一项必须是根商品");
        }

        Set<Long> productIds = new HashSet<>();
        Set<String> skus = new HashSet<>();

        for (Item item : items) {
            if (!productIds.add(item.productId()) || !skus.add(item.sku())) {
                throw new IllegalArgumentException(
                        "商品处理计划包含重复商品ID或SKU，商品ID="
                                + item.productId() + "，SKU=" + item.sku()
                );
            }
        }
    }

    public record Item(
            long productId,
            String sku,
            ProductType productType
    ) {

        public Item {
            if (productId <= 0
                    || sku == null
                    || sku.isBlank()
                    || sku.codePointCount(0, sku.length()) > 50
                    || productType == null) {
                throw new IllegalArgumentException(
                        "无效的商品处理计划项，商品ID=" + productId
                );
            }
        }
    }
}