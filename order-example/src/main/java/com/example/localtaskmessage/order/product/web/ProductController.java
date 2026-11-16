package com.example.localtaskmessage.order.product.web;

import com.example.localtaskmessage.order.product.application.ProductPlanService;
import com.example.localtaskmessage.order.product.application.ProductPlanService.TreePlanPreview;
import com.example.localtaskmessage.order.product.application.ProductRelationConflictException;
import com.example.localtaskmessage.order.product.application.ProductRelationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductPlanService planService;
    private final ProductRelationService relationService;

    public ProductController(
            ProductPlanService planService,
            ProductRelationService relationService
    ) {
        this.planService = planService;
        this.relationService = relationService;
    }

    @GetMapping("/{rootProductId}/tree-plan")
    public TreePlanPreview preview(
            @PathVariable("rootProductId") long rootProductId
    ) {
        return planService.preview(rootProductId);
    }

    @PostMapping("/{parentProductId}/children/{childProductId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addRelation(
            @PathVariable("parentProductId") long parentProductId,
            @PathVariable("childProductId") long childProductId,
            @RequestParam(name = "quantity", defaultValue = "1") int quantity,
            @RequestParam(name = "sortOrder", defaultValue = "0") int sortOrder
    ) {
        relationService.addRelation(
                parentProductId, childProductId, quantity, sortOrder
        );
    }

    @ExceptionHandler(ProductRelationConflictException.class)
    public ProblemDetail duplicateRelation(ProductRelationConflictException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail invalidTreeOrPlan(IllegalArgumentException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }
}
