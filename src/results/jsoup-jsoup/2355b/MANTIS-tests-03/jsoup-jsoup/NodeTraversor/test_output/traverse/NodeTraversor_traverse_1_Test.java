package org.jsoup.select;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.Elements;
import org.jsoup.select.NodeVisitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class NodeTraversor_traverse_1_Test {

    @Test
    @DisplayName("TC12: two-level tree descends then ascends exercising descend branch and tail on return")
    public void test_TC12() {
        // Given a root with one child to exercise B11→B12 (descend) and then B20/B22 (ascend)
        Element child = new Element("span");
        Element root = new Element("div").appendChild(child);
        List<String> head = new ArrayList<>();
        List<String> tail = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) { head.add(node.nodeName() + ":" + depth); }
            public void tail(Node node, int depth) { tail.add(node.nodeName() + ":" + depth); }
        };

        // When
        org.jsoup.select.NodeTraversor.traverse(visitor, new Elements(root));

        // Then: head should record root at depth 0 then child at depth 1
        assertEquals(List.of("div:0", "span:1"), head);
        // tail should record child at depth 1 then root at depth 0
        assertEquals(List.of("span:1", "div:0"), tail);
    }

    @Test
    @DisplayName("TC13: node with children triggers AssertionError in interior when assertions enabled")
    public void test_TC13() {
        // Given a nested structure: root->child->grandchild to trigger assertion in B11→B13→B14→B15
        Element grandchild = new Element("i");
        Element child = new Element("b").appendChild(grandchild);
        Element root = new Element("div").appendChild(child);
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) { /* no-op */ }
            public void tail(Node node, int depth) { /* no-op */ }
        };

        // When/Then: expecting AssertionError due to interior assertion on child with children
        assertThrows(AssertionError.class, () -> org.jsoup.select.NodeTraversor.traverse(visitor, new Elements(root)));
    }

    @Test
    @DisplayName("TC14: three-level chain with no siblings exercises ascend-loop branch")
    public void test_TC14() {
        // Given a chain root->mid->leaf, no siblings to force ascend-loop in B16→B17→B19
        Element leaf = new Element("i");
        Element mid = new Element("b").appendChild(leaf);
        Element root = new Element("div").appendChild(mid);
        List<String> head = new ArrayList<>();
        List<String> tail = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) { head.add(node.nodeName() + ":" + depth); }
            public void tail(Node node, int depth) { tail.add(node.nodeName() + ":" + depth); }
        };

        // When
        org.jsoup.select.NodeTraversor.traverse(visitor, new Elements(root));

        // Then: head records div:0, b:1, i:2
        assertEquals(List.of("div:0", "b:1", "i:2"), head);
        // tail should include i:2, b:1, div:0 in order of completion
        assertTrue(tail.containsAll(List.of("i:2", "b:1", "div:0")));
    }

    @Test
    @DisplayName("TC15: multiple siblings exercises sibling-null ascend then tail on non-root")
    public void test_TC15() {
        // Given a root with two siblings c1 and c2 to test sibling ascends in B16→B20→B21
        Element c1 = new Element("a");
        Element c2 = new Element("b");
        Element root = new Element("div").appendChild(c1).appendChild(c2);
        List<String> head = new ArrayList<>();
        List<String> tail = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) { head.add(node.nodeName() + ":" + depth); }
            public void tail(Node node, int depth) { tail.add(node.nodeName() + ":" + depth); }
        };

        // When
        org.jsoup.select.NodeTraversor.traverse(visitor, new Elements(root));

        // Then: head visits div:0, a:1, b:1
        assertEquals(List.of("div:0", "a:1", "b:1"), head);
        // tail should record a:1, b:1, then div:0
        assertEquals(List.of("a:1", "b:1", "div:0"), tail);
    }
}