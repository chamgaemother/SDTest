package org.jsoup.select;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NodeTraversor_traverse_2_Test {

    @Test
    @DisplayName("visitor.head removes first of two siblings exercises removal branch where nextSibling != null")
    public void test_TC12() {
        // GIVEN root with two children A and B
        Element root = new Element("div");
        Element a = new Element("p");
        Element b = new Element("span");
        root.appendChild(a);
        root.appendChild(b);
        // Capture traversal calls
        List<String> calls = new ArrayList<>();
        org.jsoup.select.NodeVisitor visitor = new org.jsoup.select.NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                // removal branch: remove A upon head to exercise removal of first sibling (nextSibling != null)
                if (node == a) {
                    node.remove();
                }
                calls.add("head:" + node.nodeName() + ":" + depth);
            }
            @Override
            public void tail(Node node, int depth) {
                calls.add("tail:" + node.nodeName() + ":" + depth);
            }
        };

        // WHEN
        NodeTraversor.traverse(visitor, new Elements(root)); // Updated to match method signature

        // THEN only B is visited (A removed before tail)
        List<String> expected = List.of(
            "head:div:0",
            "head:span:1",
            "tail:span:1",
            "tail:div:0"
        );
        assertEquals(expected, calls);
    }

    @Test
    @DisplayName("visitor.head replaces only child triggers replacement branch and continues traversal")
    public void test_TC13() {
        // GIVEN root with single child C
        Element root = new Element("div");
        Element c = new Element("p");
        root.appendChild(c);
        // Capture traversal calls
        List<String> calls = new ArrayList<>();
        org.jsoup.select.NodeVisitor visitor = new org.jsoup.select.NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                // replacement branch: replace C with D to exercise replace logic
                if (node == c) {
                    Element d = new Element("span");
                    node.replaceWith(d);
                }
                calls.add("head:" + node.nodeName() + ":" + depth);
            }
            @Override
            public void tail(Node node, int depth) {
                calls.add("tail:" + node.nodeName() + ":" + depth);
            }
        };

        // WHEN
        NodeTraversor.traverse(visitor, new Elements(root)); // Updated to match method signature

        // THEN replacement D is traversed instead of C
        List<String> expected = List.of(
            "head:div:0",
            "head:span:1",
            "tail:span:1",
            "tail:div:0"
        );
        assertEquals(expected, calls);
    }

    @Test
    @DisplayName("null Elements list throws IllegalArgumentException at Validate.notNull(elements)")
    public void test_TC14() {
        // GIVEN a valid NodeVisitor and null Elements to exercise null check
        org.jsoup.select.NodeVisitor visitor = new org.jsoup.select.NodeVisitor() {
            @Override
            public void head(Node n, int d) {}
            @Override
            public void tail(Node n, int d) {}
        };
        Elements elements = null;

        // WHEN / THEN IllegalArgumentException is thrown
        assertThrows(IllegalArgumentException.class, () -> {
            NodeTraversor.traverse(visitor, new Elements(elements)); // Updated to match method signature
        });
    }
}