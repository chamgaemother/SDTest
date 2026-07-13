package org.jsoup.select;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class NodeTraversor_traverse_1_Test {

    @Test
    @DisplayName("TC11: two-level tree: root with one child exercises descend and ascend loop")
    public void test_TC11() {
        // Given: root has one child to trigger descend (childNodeSize>0) and then ascend loop when no siblings
        Element root = new Element("div");
        Element child = new Element("p");
        root.appendChild(child);
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) { calls.add("H:" + node.nodeName() + ":" + depth); }
            public void tail(Node node, int depth) { calls.add("T:" + node.nodeName() + ":" + depth); }
        };
        // When
        NodeTraversor.traverse(visitor, root);
        // Then: head on root (0), head on child (1), tail on child (1), tail on root (0)
        assertEquals(
            List.of("H:div:0", "H:p:1", "T:p:1", "T:div:0"),
            calls
        );
    }

    @Test
    @DisplayName("TC12: deep nested tree traversal exercises multiple descents and ascents")
    public void test_TC12() {
        // Given: root->a->b nested to depth 2 to test multiple descend loops and ascents
        Element root = new Element("div");
        Element a = new Element("a");
        Element b = new Element("b");
        root.appendChild(a);
        a.appendChild(b);
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) { calls.add("H:" + node.nodeName() + ":" + depth); }
            public void tail(Node node, int depth) { calls.add("T:" + node.nodeName() + ":" + depth); }
        };
        // When
        NodeTraversor.traverse(visitor, root);
        // Then: correct head/tail order for depths 0,1,2
        assertEquals(
            List.of(
                "H:div:0", "H:a:1", "H:b:2", 
                "T:b:2", "T:a:1", "T:div:0"
            ),
            calls
        );
    }

    @Test
    @DisplayName("TC13: visitor replaces node in head triggers replaced branch and continues traversal")
    public void test_TC13() {
        // Given: root with one child such that head replaces child node -> triggers replace branch (origSize stays same)
        Element root = new Element("div");
        Element child = new Element("p");
        root.appendChild(child);
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) {
                calls.add("head:" + node.nodeName() + ":" + depth);
                if ("p".equals(node.nodeName())) {
                    node.replaceWith(new Element("span"));
                }
            }
            public void tail(Node node, int depth) {
                calls.add("tail:" + node.nodeName() + ":" + depth);
            }
        };
        // When
        NodeTraversor.traverse(visitor, root);
        // Then: head div, head p, head span (replacement), tail span, tail div
        assertEquals(
            List.of(
                "head:div:0", "head:p:1", "head:span:1", 
                "tail:span:1", "tail:div:0"
            ),
            calls
        );
    }

    @Test
    @DisplayName("TC14: elements overload with single element invokes one-node traversal")
    public void test_TC14() {
        // Given: Elements with one element, to cover overload iterate once
        Element el = new Element("div");
        Elements elements = new Elements(el);
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) { calls.add("H:" + node.nodeName() + ":" + depth); }
            public void tail(Node node, int depth) { calls.add("T:" + node.nodeName() + ":" + depth); }
        };
        // When
        NodeTraversor.traverse(visitor, elements);
        // Then: single element head and tail at depth 0
        assertEquals(
            List.of("H:div:0", "T:div:0"),
            calls
        );
    }

    @Test
    @DisplayName("TC15: elements overload with multiple elements invokes traversal for each")
    public void test_TC15() {
        // Given: Elements with two siblings to trigger two iterations of overload
        Element a = new Element("a");
        Element b = new Element("b");
        Elements elements = new Elements(a, b);
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) { calls.add("H:" + node.nodeName() + ":" + depth); }
            public void tail(Node node, int depth) { calls.add("T:" + node.nodeName() + ":" + depth); }
        };
        // When
        NodeTraversor.traverse(visitor, elements);
        // Then: first element head/tail then second element head/tail
        assertEquals(
            List.of(
                "H:a:0", "T:a:0", 
                "H:b:0", "T:b:0"
            ),
            calls
        );
    }
}