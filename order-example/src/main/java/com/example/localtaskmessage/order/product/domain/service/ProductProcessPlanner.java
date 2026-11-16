package com.example.localtaskmessage.order.product.domain.service;

import com.example.localtaskmessage.order.product.domain.model.Product;
import com.example.localtaskmessage.order.product.domain.model.ProductProcessPlan;
import com.example.localtaskmessage.order.product.domain.model.ProductTreeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ProductProcessPlanner {

    public ProductProcessPlan buildPlan(ProductTreeNode tree) {
        Product rootProduct = tree.product();

        List<ProductTreeNode> postOrderNodes = new ArrayList<>();
        collectPostOrder(tree, postOrderNodes);

        Set<Long> seenProductIds = new HashSet<>();
        Map<String, ProductTreeNode> firstNodeBySku = new HashMap<>();
        List<ProductProcessPlan.Item> items = new ArrayList<>();

        for (ProductTreeNode node : postOrderNodes) {
            Product product = node.product();

            if (product.id() != rootProduct.id()
                    && product.sku().equals(rootProduct.sku())) {
                throw new IllegalArgumentException(
                        "根商品与后代商品的 SKU 重复，SKU=" + product.sku()
                                + "，根商品ID=" + rootProduct.id()
                                + "，后代商品ID=" + product.id()
                );
            }

            if (!seenProductIds.add(product.id())) {
                continue;
            }

            ProductTreeNode first = firstNodeBySku.putIfAbsent(
                    product.sku(), node
            );

            if (first == null) {
                items.add(new ProductProcessPlan.Item(
                        product.id(),
                        product.sku(),
                        product.type()
                ));
            } else if (first.product().type() != product.type()
                    || !childDefinitions(first).equals(childDefinitions(node))) {
                throw new IllegalArgumentException(
                        "同 SKU 商品的类型或组合定义冲突，SKU=" + product.sku()
                                + "，商品ID=" + first.product().id()
                                + " 和 " + product.id()
                );
            }
        }

        return new ProductProcessPlan(rootProduct.id(), items);
    }

    private static void collectPostOrder(
            ProductTreeNode node,
            List<ProductTreeNode> result
    ) {
        for (ProductTreeNode child : node.children()) {
            collectPostOrder(child, result);
        }

        result.add(node);
    }

    private static List<ChildDefinition> childDefinitions(
            ProductTreeNode node
    ) {
        return node.children().stream()
                .map(child -> new ChildDefinition(
                        child.product().sku(),
                        child.incomingRelation().quantity()
                ))
                .toList();
    }

    private record ChildDefinition(String sku, int quantity) {
    }
}
