package com.example.localtaskmessage.order.product;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductProcessRecordRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductProcessRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ProductProcessRecord> findByOrderNo(String orderNo) {
        return jdbcTemplate.query("""
                SELECT task_id, order_no, process_order,
                       product_id, sku, product_type
                FROM order_product_process_record
                WHERE order_no = ?
                ORDER BY process_order ASC, id ASC
                """,
                (rs, rowNum) -> new ProductProcessRecord(
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

    public record ProductProcessRecord(
            String taskId,
            String orderNo,
            int processOrder,
            long productId,
            String sku,
            ProductType productType
    ) {
    }
}