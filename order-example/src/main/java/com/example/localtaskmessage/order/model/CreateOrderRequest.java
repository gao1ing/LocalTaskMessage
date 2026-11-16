package com.example.localtaskmessage.order.model;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 下单接口接收的订单信息。
 *
 * @param orderNo 业务订单号，最多 64 个字符，数据库要求唯一
 * @param userId 用户 ID，必须大于 0
 * @param payAmount 订单金额，最多 8 位整数和 2 位小数
 * @param rootProductId 可选的根商品 ID，传入时必须大于 0
 */
public record CreateOrderRequest(

        @NotBlank
        @Size(max = 64)
        String orderNo,

        @NotNull
        @Positive
        Long userId,

        @NotNull
        @Positive
        @Digits(integer = 8, fraction = 2)
        BigDecimal payAmount,

        @Positive
        Long rootProductId
) {
}