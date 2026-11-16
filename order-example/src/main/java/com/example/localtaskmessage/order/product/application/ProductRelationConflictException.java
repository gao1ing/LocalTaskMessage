package com.example.localtaskmessage.order.product.application;

public final class ProductRelationConflictException extends RuntimeException {

    public ProductRelationConflictException(Throwable cause) {
        super("这对父子商品已有有效关联", cause);
    }
}
