package org.jsoup.select;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class NodeTraversor_traverse_2_Test {

    @Test
    @DisplayName("visitor.head removes the only child at depth=1 (nextSibling null) triggers remove-without-sibling branch B9→B10")
    public void test_TC15() {
        // GIVEN: a root element with exactly one child to traverse, so nextSibling is null at child
        Element root = new Element("div");
        Element child = new Element("p");
        root.appendChild(child);

        // Visitor that removes the node when depth == 1 (child)
        class Remover implements NodeVisitor {
            int headCount = 0;
            int tailCount = 0;

            @Override
            public void head(Node node, int depth) {
                headCount++;
                // At depth 1 (the only child), trigger removal branch where nextSibling == null
                if (depth == 1) {
                    node.remove();
                }
            }

            @Override
            public void tail(Node node, int depth) {
                tailCount++;
            }
        }
        Remover vis = new Remover();

        // WHEN: traverse the nodes
        NodeTraversor.traverse(vis, root);

        // THEN: only root remains, head invoked for root and child, tail only for root
        assertAll("Verify removal and visit counts",
            () -> assertEquals(0, root.childNodeSize(), "Child should be removed from root"),
            () -> assertEquals(2, vis.headCount, "head should be called for root and removed child"),
            () -> assertEquals(1, vis.tailCount, "tail should be called only for the root at depth 0")
        );
    }
}