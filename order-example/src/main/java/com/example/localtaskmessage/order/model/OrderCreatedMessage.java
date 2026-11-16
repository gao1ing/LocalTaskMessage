package com.example.localtaskmessage.order.model;

import com.example.localtaskmessage.order.product.ProductProcessPlan;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 消费端读取的订单创建消息。
 *
 * @param taskId 本地消息任务 ID，同一任务重发时复用
 * @param orderNo 消息关联的订单号
 * @param productPlan 下单时生成的商品处理计划，旧消息没有此字段
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderCreatedMessage(

        @NotBlank
        @Size(max = 64)
        String taskId,

        @NotBlank
        @Size(max = 64)
        String orderNo,

        ProductProcessPlan productPlan
) {

    public static final String TOPIC = "order_created";
}