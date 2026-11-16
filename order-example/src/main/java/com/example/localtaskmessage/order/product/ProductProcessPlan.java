package com.example.localtaskmessage.order.product;

import java.util.List;

public record ProductProcessPlan(
        long rootProductId,
        List<Item> items
) {

    public ProductProcessPlan {
        items = List.copyOf(items);
    }

    public record Item(
            long productId,
            String sku,
            ProductType productType
    ) {
    }
}