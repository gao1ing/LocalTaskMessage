package com.example.localtaskmessage.order.service;

import com.example.localtaskmessage.order.model.CreateOrderRequest;
import com.example.localtaskmessage.order.model.OrderCreatedMessage;
import com.example.localtaskmessage.order.product.ProductProcessPlan;
import com.example.localtaskmessage.order.product.ProductProcessPlanService;
import com.example.localtaskmessage.order.product.ProductTreeNode;
import com.example.localtaskmessage.order.product.ProductTreeService;
import com.example.localtaskmessage.taskmessage.api.LocalTaskMessage;
import com.example.localtaskmessage.taskmessage.api.TaskMessageCommand;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    private final JdbcTemplate jdbcTemplate;
    private final JsonMapper jsonMapper;
    private final ProductTreeService productTreeService;
    private final ProductProcessPlanService productProcessPlanService;

    public OrderService(
            JdbcTemplate jdbcTemplate,
            JsonMapper jsonMapper,
            ProductTreeService productTreeService,
            ProductProcessPlanService productProcessPlanService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonMapper = jsonMapper;
        this.productTreeService = productTreeService;
        this.productProcessPlanService = productProcessPlanService;
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
            ProductTreeNode tree = productTreeService.buildTree(rootProductId);
            ProductProcessPlan plan = productProcessPlanService.buildPlan(tree);
            messageBody.put("productPlan", plan);
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
}