package com.example.localtaskmessage.order.web;

import com.example.localtaskmessage.order.model.CreateOrderRequest;
import com.example.localtaskmessage.order.model.OrderProductProcessRecord;
import com.example.localtaskmessage.order.service.OrderService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 接收下单请求，校验参数后调用订单服务。
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 返回 201 表示订单和消息任务已提交，消息随后发送并消费。
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> createOrder(@Valid @RequestBody CreateOrderRequest request) {

        orderService.createOrder(request);

        return Map.of("orderNo", request.orderNo());

    }

    @GetMapping("/{orderNo}/product-process-records")
    public List<OrderProductProcessRecord> productProcessRecords(
            @PathVariable("orderNo") String orderNo
    ) {
        return orderService.findProductProcessRecords(orderNo);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail invalidOrderProduct(IllegalArgumentException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

}
