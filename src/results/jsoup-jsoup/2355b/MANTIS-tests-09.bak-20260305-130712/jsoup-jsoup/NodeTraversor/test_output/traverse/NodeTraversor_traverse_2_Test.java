package org.jsoup.select;

import org.jsoup.nodes.Node;
import org.jsoup.nodes.Elements;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NodeTraversor_traverse_2_Test {

    @Test
    @DisplayName("traverse(elements) throws IllegalArgumentException when elements list is null")
    public void test_TC14_O2() {
        // GIVEN: a non-null NodeVisitor and a null Elements list
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                // no-op
            }
            @Override
            public void tail(Node node, int depth) {
                // no-op
            }
        };
        Elements elements = null;
        // WHEN & THEN: calling traverse with null elements must fail null-check and throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            NodeTraversor.traverse(visitor, elements);
        });
    }
}