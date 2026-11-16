package com.example.localtaskmessage.order.repository;

import com.example.localtaskmessage.order.model.OrderProductProcessRecord;
import com.example.localtaskmessage.order.product.domain.model.ProductType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderProductProcessRecordRepository {

    private final JdbcTemplate jdbcTemplate;

    public OrderProductProcessRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(OrderProductProcessRecord processRecord) {
        jdbcTemplate.update("""
                INSERT INTO order_product_process_record (
                    task_id, order_no, process_order, product_id, sku, product_type
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                processRecord.taskId(),
                processRecord.orderNo(),
                processRecord.processOrder(),
                processRecord.productId(),
                processRecord.sku(),
                processRecord.productType().code()
        );
    }

    public List<OrderProductProcessRecord> findByOrderNo(String orderNo) {
        return jdbcTemplate.query("""
                SELECT task_id, order_no, process_order,
                       product_id, sku, product_type
                FROM order_product_process_record
                WHERE order_no = ?
                ORDER BY process_order ASC, id ASC
                """,
                (rs, rowNum) -> new OrderProductProcessRecord(
                        rs.getString("task_id"),
                        rs.getString("order_no"),
                        rs.getInt("process_order"),
                        rs.getLong("product_id"),
                        rs.getString("sku"),
                        ProductType.fromCode(rs.getString("product_type"))
                ),
                orderNo
        );
    }
}
