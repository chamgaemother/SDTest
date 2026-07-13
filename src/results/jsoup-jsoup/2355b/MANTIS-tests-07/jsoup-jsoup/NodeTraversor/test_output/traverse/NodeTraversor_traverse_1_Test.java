package org.jsoup.select;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Generated tests for NodeTraversor.traverse(NodeVisitor, Node) based on scenarios.
 */
public class NodeTraversor_traverse_1_Test {

    @Test
    @DisplayName("TC10: Child replaced in head triggers the replaced branch (origSize==childNodeSize) then correct head/tail sequence")
    public void test_TC10() {
        // GIVEN root with one child; replacement element for branch: origSize == childNodeSize
        Element root = new Element(Tag.valueOf("div"), "");
        Element child = new Element(Tag.valueOf("p"), "");
        root.appendChild(child);
        Element repl = new Element(Tag.valueOf("span"), "");
        List<String> order = new ArrayList<>();
        org.jsoup.select.NodeVisitor v = new org.jsoup.select.NodeVisitor() {
            @Override
            public void head(Node n, int depth) {
                // When head sees the original child, replace it -> triggers replace-branch
                order.add("H" + n.nodeName());
                if (n == child) {
                    n.replaceWith(repl);
                }
            }
            @Override
            public void tail(Node n, int depth) {
                order.add("T" + n.nodeName());
            }
        };

        // WHEN
        org.jsoup.select.NodeTraversor.traverse(v, root);

        // THEN: expect head on original child, then head and tail on replacement, then tail on root
        List<String> expected = List.of("Hp", "Hspan", "Tspan", "Tdiv");
        assertEquals(expected, order, 
            "Should visit original child (Hp), then replacement span head/tail, and then root tail");
    }

    @Test
    @DisplayName("TC11: Descend into first child, then sibling exists at same depth to exercise sibling branch")
    public void test_TC11() {
        // GIVEN root with two children at same depth (c1, c2) to trigger inner while sibling branch
        Element root = new Element(Tag.valueOf("ul"), "");
        Element c1 = new Element(Tag.valueOf("li"), "");
        Element c2 = new Element(Tag.valueOf("li"), "");
        root.appendChild(c1);
        root.appendChild(c2);
        List<String> seq = new ArrayList<>();
        org.jsoup.select.NodeVisitor v = new org.jsoup.select.NodeVisitor() {
            @Override
            public void head(Node n, int depth) {
                // depth distinguishes root (0) and children (1)
                seq.add("H" + depth + ":" + n.nodeName());
            }
            @Override
            public void tail(Node n, int depth) {
                seq.add("T" + depth + ":" + n.nodeName());
            }
        };

        // WHEN
        org.jsoup.select.NodeTraversor.traverse(v, root);

        // THEN: H0-root, descend to first child H1:c1, no children so tail T1:c1, sibling at same depth H1:c2, tail T1:c2, then T0:root
        List<String> expected = List.of(
            "H0:ul",
            "H1:li",
            "T1:li",
            "H1:li",
            "T1:li",
            "T0:ul"
        );
        assertEquals(expected, seq,
            "Should traverse root, first child, tail it, then sibling, tail it, then root tail");
    }
}