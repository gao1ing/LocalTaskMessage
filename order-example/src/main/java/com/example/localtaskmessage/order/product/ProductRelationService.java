package com.example.localtaskmessage.order.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductRelationService {

    private final ProductRepository productRepository;
    private final ProductTreeService treeService;
    private final ProductProcessPlanService planService;

    public ProductRelationService(
            ProductRepository productRepository,
            ProductTreeService treeService,
            ProductProcessPlanService planService
    ) {
        this.productRepository = productRepository;
        this.treeService = treeService;
        this.planService = planService;
    }

    @Transactional
    public void addRelation(
            long parentProductId,
            long childProductId,
            int quantity,
            int sortOrder
    ) {
        if (parentProductId <= 0 || childProductId <= 0) {
            throw new IllegalArgumentException("父商品和子商品 ID 必须大于 0");
        }

        if (parentProductId == childProductId) {
            throw new IllegalArgumentException("商品不能关联自身");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("关联数量必须大于 0");
        }

        // 先锁定有效商品，使通过此接口的关联串行写入
        List<Long> activeProductIds = productRepository.lockActiveProductIds();

        Product parent = productRepository.findById(parentProductId)
                .filter(product -> activeProductIds.contains(product.id()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "父商品不存在或已删除，商品ID=" + parentProductId
                ));


        if (!parent.type().isComposite()) {
            throw new IllegalArgumentException("父商品必须是同款或套装");
        }

        if (!activeProductIds.contains(childProductId)) {
            throw new IllegalArgumentException(
                    "子商品不存在或已删除，商品ID=" + childProductId
            );
        }

        productRepository.insertActiveRelation(
                parentProductId, childProductId, quantity, sortOrder
        );

        // 每个有效商品都可能作为下单入口，校验它能否生成合法计划。
        for (long rootProductId : activeProductIds) {
            ProductTreeNode tree = treeService.buildTree(rootProductId);
            planService.buildPlan(tree);
        }
    }

}
