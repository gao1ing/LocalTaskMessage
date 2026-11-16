package com.example.localtaskmessage.order.model;

import com.example.localtaskmessage.order.product.domain.model.ProductType;

public record OrderProductProcessRecord(
        String taskId,
        String orderNo,
        int processOrder,
        long productId,
        String sku,
        ProductType productType
) {
}
