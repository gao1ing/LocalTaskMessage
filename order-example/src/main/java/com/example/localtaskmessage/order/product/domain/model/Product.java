package com.example.localtaskmessage.order.product.domain.model;

public record Product(
        long id,
        String sku,
        String productName,
        ProductType type
) {
}
