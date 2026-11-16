package com.example.localtaskmessage.order.service;

import com.example.localtaskmessage.order.model.OrderCreatedMessage;
import com.example.localtaskmessage.order.product.ProductProcessPlan;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 幂等消费订单创建消息，按计划记录商品处理顺序。
 */
@Service
public class OrderCreatedConsumeService {

    public static final String GROUP_NAME = "order_created_consumer";

    private final JdbcTemplate jdbcTemplate;

    public OrderCreatedConsumeService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 首次成功处理返回 true，重复投递返回 false。
     */
    @Transactional
    public boolean consume(OrderCreatedMessage event) {
        try {
            jdbcTemplate.update("""
                    INSERT INTO message_consume_record (
                        consumer_group, task_id, order_no
                    )
                    VALUES (?, ?, ?)
                    """, GROUP_NAME, event.taskId(), event.orderNo());
        } catch (DuplicateKeyException duplicate) {
            // 同一消费组已经处理过该任务，让监听器确认这次重复投递。
            return false;
        }

        writeProductProcessRecords(event);

        int updated = jdbcTemplate.update("""
                UPDATE order_consume_stat
                SET processed_count = processed_count + 1,
                    update_time = CURRENT_TIMESTAMP
                WHERE consumer_group = ?
                """, GROUP_NAME);

        if (updated != 1) {
            throw new IllegalStateException(
                    "消费统计初始化记录不存在，group=" + GROUP_NAME
            );
        }

        return true;
    }

    private void writeProductProcessRecords(OrderCreatedMessage event) {
        ProductProcessPlan plan = event.productPlan();
        if (plan == null) {
            return;
        }

        int processOrder = 1;

        for (ProductProcessPlan.Item item : plan.items()) {
            jdbcTemplate.update("""
                    INSERT INTO order_product_process_record (
                        task_id, order_no, process_order, product_id, sku, product_type
                    )
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    event.taskId(),
                    event.orderNo(),
                    processOrder,
                    item.productId(),
                    item.sku(),
                    item.productType().code()
            );

            processOrder++;
        }
    }
}