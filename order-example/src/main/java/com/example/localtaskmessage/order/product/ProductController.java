package com.example.localtaskmessage.order.product;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DuplicateKeyException;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductTreeService treeService;
    private final ProductProcessPlanService planService;
    private final ProductRelationService relationService;

    public ProductController(
            ProductTreeService treeService,
            ProductProcessPlanService planService,
            ProductRelationService relationService
    ) {
        this.treeService = treeService;
        this.planService = planService;
        this.relationService = relationService;
    }


    @GetMapping("/{rootProductId}/tree-plan")
    public TreePlanPreview preview(
            @PathVariable("rootProductId") long rootProductId
    ) {
        ProductTreeNode tree = treeService.buildTree(rootProductId);
        ProductProcessPlan plan = planService.buildPlan(tree);

        return new TreePlanPreview(tree, plan);
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

    @ExceptionHandler(DuplicateKeyException.class)
    public ProblemDetail duplicateRelation() {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "这对父子商品已有有效关联"
        );
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