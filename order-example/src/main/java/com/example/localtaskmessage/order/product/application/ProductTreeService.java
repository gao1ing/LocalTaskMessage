package com.example.localtaskmessage.order.product.application;

import com.example.localtaskmessage.order.product.domain.model.Product;
import com.example.localtaskmessage.order.product.domain.model.ProductRelation;
import com.example.localtaskmessage.order.product.domain.model.ProductTreeNode;
import com.example.localtaskmessage.order.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProductTreeService {

    private static final int MAX_DEPTH = 5;
    private static final int MAX_EXPANDED_NODES = 100;

    private final ProductRepository productRepository;

    public ProductTreeService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public ProductTreeNode buildTree(long rootProductId) {
        BuildContext context = new BuildContext();

        return buildNode(
                rootProductId,
                null,
                0,
                context
        );
    }

    private ProductTreeNode buildNode(
            long productId,
            ProductRelation incomingRelation,
            int depth,
            BuildContext context
    ) {
        if (productId <= 0) {
            throw new IllegalArgumentException("商品ID必须大于0");
        }

        if (depth > MAX_DEPTH) {
            throw new IllegalArgumentException(
                    "商品树超过最大子项层数 " + MAX_DEPTH
                            + "，商品ID=" + productId
            );
        }

        if (!context.path.add(productId)) {
            throw new IllegalArgumentException(
                    "检测到循环关联，当前路径=" + context.path
                            + "，再次访问=" + productId
            );
        }

        try {
            context.expandedNodes++;

            if (context.expandedNodes > MAX_EXPANDED_NODES) {
                throw new IllegalArgumentException(
                        "商品树展开节点数超过上限 "
                                + MAX_EXPANDED_NODES
                );
            }

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "商品不存在或已删除，商品ID=" + productId
                    ));

            if (product.sku() == null || product.sku().isBlank()) {
                throw new IllegalArgumentException(
                        "商品SKU不能为空，商品ID=" + productId
                );
            }

            if (product.type() == null) {
                throw new IllegalArgumentException(
                        "商品类型不能为空，商品ID=" + productId
                );
            }

            List<ProductRelation> relations =
                    productRepository.findChildRelations(productId);

            if (!product.type().isComposite() && !relations.isEmpty()) {
                throw new IllegalArgumentException(
                        "普通商品不能包含子商品，商品ID=" + productId
                );
            }

            List<ProductTreeNode> children = new ArrayList<>();

            for (ProductRelation relation : relations) {
                if (relation.childProductId() == productId) {
                    throw new IllegalArgumentException(
                            "商品不能关联自身，关联ID=" + relation.id()
                    );
                }

                if (relation.quantity() <= 0) {
                    throw new IllegalArgumentException(
                            "关联数量必须大于0，关联ID=" + relation.id()
                    );
                }

                ProductTreeNode child = buildNode(
                        relation.childProductId(),
                        relation,
                        depth + 1,
                        context
                );

                children.add(child);
            }

            return new ProductTreeNode(
                    product,
                    incomingRelation,
                    children
            );
        } finally {
            context.path.remove(productId);
        }
    }

    private static final class BuildContext {

        private final Set<Long> path = new LinkedHashSet<>();
        private int expandedNodes;
    }
}
