package com.example.localtaskmessage.order.product;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductTreeService treeService;
    private final ProductProcessPlanService planService;

    public ProductController(
            ProductTreeService treeService,
            ProductProcessPlanService planService
    ) {
        this.treeService = treeService;
        this.planService = planService;
    }

    @GetMapping("/{rootProductId}/tree-plan")
    public TreePlanPreview preview(
            @PathVariable("rootProductId") long rootProductId
    ) {
        ProductTreeNode tree = treeService.buildTree(rootProductId);
        ProductProcessPlan plan = planService.buildPlan(tree);

        return new TreePlanPreview(tree, plan);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail invalidTreeOrPlan(IllegalArgumentException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    public record TreePlanPreview(
            ProductTreeNode tree,
            ProductProcessPlan plan
    ) {
    }
}