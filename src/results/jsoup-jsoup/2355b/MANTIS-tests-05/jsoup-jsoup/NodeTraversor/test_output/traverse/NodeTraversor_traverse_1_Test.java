package org.jsoup.select;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NodeTraversor_traverse_1_Test {

    @Test
    @DisplayName("descends into single child: triggers B11→B12 twice for depth-first traversal")
    public void test_TC12() {
        // GIVEN: root has one child, so traverse will descend into child (childNodeSize>0)
        Element root = new Element("div");
        Element child = new Element("span");
        root.appendChild(child);
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                // depth matches node.siblingIndex() to check head order
                calls.add("head:" + node.nodeName() + ":" + depth);
            }
            @Override
            public void tail(Node node, int depth) {
                calls.add("tail:" + node.nodeName() + ":" + depth);
            }
        };
        // WHEN
        org.jsoup.select.NodeTraversor.traverse(visitor, new org.jsoup.select.Elements(root)); // Updated to use Elements
        // THEN: expect head(div:0), head(span:1), tail(span:1), tail(div:0)
        List<String> expected = List.of(
            "head:div:0",
            "head:span:1",
            "tail:span:1",
            "tail:div:0"
        );
        assertEquals(expected, calls, "Traversal should record correct head/tail calls for single-child depth-first");
    }

    @Test
    @DisplayName("removes first of two siblings in head: executes removal path B7→B9→B10 when nextSibling exists")
    public void test_TC13() {
        // GIVEN: root has two children; visitor.head removes first child when visited
        Element root = new Element("ul");
        Element li1 = new Element("li");
        Element li2 = new Element("li");
        root.appendChild(li1);
        root.appendChild(li2);
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                calls.add("head:" + node.nodeName() + ":" + depth);
                if (node == li1) {
                    // removal in head triggers the removal branch when nextSibling exists
                    node.remove();
                }
            }
            @Override
            public void tail(Node node, int depth) {
                calls.add("tail:" + node.nodeName() + ":" + depth);
            }
        };
        // WHEN
        org.jsoup.select.NodeTraversor.traverse(visitor, new org.jsoup.select.Elements(root)); // Updated to use Elements
        // THEN: first child was removed after head, traversal continues to second; last call is tail:ul:0
        assertTrue(calls.contains("head:li:1"), "First child should be visited and removed");
        assertTrue(calls.contains("tail:li:1"), "Second child should be visited and tailed");
        assertEquals("tail:ul:0", calls.get(calls.size() - 1), "Root should be tailed last");
    }

    @Test
    @DisplayName("visits multiple siblings sequentially: triggers B16→B20→B22 for two child nodes")
    public void test_TC14() {
        // GIVEN: root has two child elements without children; no removals, so sequential siblings path
        Element root = new Element("ul");
        Element a = new Element("li");
        Element b = new Element("li");
        root.appendChild(a);
        root.appendChild(b);
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                calls.add("head:" + node.nodeName() + ":" + depth);
            }
            @Override
            public void tail(Node node, int depth) {
                calls.add("tail:" + node.nodeName() + ":" + depth);
            }
        };
        // WHEN
        org.jsoup.select.NodeTraversor.traverse(visitor, new org.jsoup.select.Elements(root)); // Updated to use Elements
        // THEN: expect head/ tail for root, both children, and final tail for root
        List<String> expected = List.of(
            "head:ul:0",
            "head:li:1",
            "tail:li:1",
            "head:li:1",
            "tail:li:1",
            "tail:ul:0"
        );
        assertEquals(expected, calls, "Traversal should visit siblings sequentially with correct head/tail order");
    }
}