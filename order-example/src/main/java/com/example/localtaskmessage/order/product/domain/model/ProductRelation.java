package com.example.localtaskmessage.order.product.domain.model;

public record ProductRelation(
        long id,
        long parentProductId,
        long childProductId,
        int quantity,
        int sortOrder
) {
}
