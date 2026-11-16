package com.example.localtaskmessage.order.service;

import com.example.localtaskmessage.order.model.CreateOrderRequest;
import com.example.localtaskmessage.order.model.OrderCreatedMessage;
import com.example.localtaskmessage.order.model.OrderProductProcessRecord;
import com.example.localtaskmessage.order.product.application.ProductPlanService;
import com.example.localtaskmessage.order.repository.OrderProductProcessRecordRepository;
import com.example.localtaskmessage.taskmessage.api.LocalTaskMessage;
import com.example.localtaskmessage.taskmessage.api.TaskMessageCommand;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    private final JdbcTemplate jdbcTemplate;
    private final JsonMapper jsonMapper;
    private final ProductPlanService productPlanService;
    private final OrderProductProcessRecordRepository productProcessRecordRepository;

    public OrderService(
            JdbcTemplate jdbcTemplate,
            JsonMapper jsonMapper,
            ProductPlanService productPlanService,
            OrderProductProcessRecordRepository productProcessRecordRepository
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonMapper = jsonMapper;
        this.productPlanService = productPlanService;
        this.productProcessRecordRepository = productProcessRecordRepository;
    }

    @Transactional
    @LocalTaskMessage
    public TaskMessageCommand createOrder(CreateOrderRequest request) {
        String taskId = UUID.randomUUID().toString();

        Map<String, Object> messageBody = new LinkedHashMap<>();
        messageBody.put("taskId", taskId);
        messageBody.put("orderNo", request.orderNo());
        messageBody.put("userId", request.userId());
        messageBody.put("payAmount", request.payAmount());

        Long rootProductId = request.rootProductId();
        if (rootProductId != null) {
            messageBody.put("productPlan", productPlanService.createPlan(rootProductId));
        }

        String payload = jsonMapper.writeValueAsString(messageBody);

        jdbcTemplate.update("""
                INSERT INTO biz_order (
                    order_no, user_id, pay_amount
                )
                VALUES (?, ?, ?)
                """,
                request.orderNo(),
                request.userId(),
                request.payAmount()
        );

        // 切面在外层事务提交前登记这一条消息。
        return new TaskMessageCommand(
                taskId,
                request.orderNo(),
                OrderCreatedMessage.TOPIC,
                payload
        );
    }

    public List<OrderProductProcessRecord> findProductProcessRecords(String orderNo) {
        return productProcessRecordRepository.findByOrderNo(orderNo);
    }
}
