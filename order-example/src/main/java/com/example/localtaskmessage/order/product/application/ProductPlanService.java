package com.example.localtaskmessage.order.product.application;

import com.example.localtaskmessage.order.product.domain.model.ProductProcessPlan;
import com.example.localtaskmessage.order.product.domain.model.ProductTreeNode;
import com.example.localtaskmessage.order.product.domain.service.ProductProcessPlanner;
import org.springframework.stereotype.Service;

@Service
public class ProductPlanService {

    private final ProductTreeService treeService;
    private final ProductProcessPlanner planner = new ProductProcessPlanner();

    public ProductPlanService(ProductTreeService treeService) {
        this.treeService = treeService;
    }

    public TreePlanPreview preview(long rootProductId) {
        ProductTreeNode tree = treeService.buildTree(rootProductId);
        return new TreePlanPreview(tree, planner.buildPlan(tree));
    }

    public ProductProcessPlan createPlan(long rootProductId) {
        return preview(rootProductId).plan();
    }

    public record TreePlanPreview(ProductTreeNode tree, ProductProcessPlan plan) {
    }
}
