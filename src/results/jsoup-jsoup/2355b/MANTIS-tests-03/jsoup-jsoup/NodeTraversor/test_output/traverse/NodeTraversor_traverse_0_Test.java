package org.jsoup.select;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;
import org.jsoup.select.NodeTraversor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NodeTraversor_traverse_0_Test {

    @Test
    @DisplayName("throws IllegalArgumentException when NodeVisitor is null (Validate.notNull r0)")
    public void test_TC01_O1() {
        NodeVisitor visitor = null;
        Node root = new Element("div");
        assertThrows(IllegalArgumentException.class, () -> NodeTraversor.traverse(visitor, root));
    }

    @Test
    @DisplayName("throws IllegalArgumentException when root Node is null (Validate.notNull r1)")
    public void test_TC02_O1() {
        NodeVisitor visitor = (n, d) -> { };
        Node root = null;
        assertThrows(IllegalArgumentException.class, () -> NodeTraversor.traverse(visitor, root));
    }

    @Test
    @DisplayName("single node no children calls head then tail exactly once")
    public void test_TC03_O1() {
        Element root = new Element("p");
        List<String> headCalls = new ArrayList<>();
        List<String> tailCalls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) { headCalls.add(node.nodeName() + ":" + depth); }
            public void tail(Node node, int depth) { tailCalls.add(node.nodeName() + ":" + depth); }
        };
        NodeTraversor.traverse(visitor, root);
        assertEquals(1, headCalls.size());
        assertEquals("p:0", headCalls.get(0));
        assertEquals(1, tailCalls.size());
        assertEquals("p:0", tailCalls.get(0));
    }

    @Test
    @DisplayName("two-level tree descends then ascends (one child) exercising B12 branch")
    public void test_TC04_O1() {
        Element child = new Element("span");
        Element root = new Element("div").appendChild(child);
        List<String> head = new ArrayList<>();
        List<String> tail = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) { head.add(node.nodeName() + ":" + depth); }
            public void tail(Node node, int depth) { tail.add(node.nodeName() + ":" + depth); }
        };
        NodeTraversor.traverse(visitor, root);
        assertEquals(List.of("div:0", "span:1"), head);
        assertEquals(List.of("span:1", "div:0"), tail);
    }

    @Test
    @DisplayName("visitor.head removes node triggers removal branch (origSize!=childNodeSize)")
    public void test_TC05_O1() {
        Element child = new Element("b");
        Element root = new Element("div").appendChild(child);
        List<String> tail = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) {
                if (node == child) {
                    child.remove();
                }
            }
            public void tail(Node node, int depth) {
                tail.add(node.nodeName() + ":" + depth);
            }
        };
        NodeTraversor.traverse(visitor, root);
        assertEquals(0, root.childNodeSize());
        assertFalse(tail.contains("b:1"));
    }

    @Test
    @DisplayName("visitor.head replaces node triggers replacement branch (origSize==childNodeSize)")
    public void test_TC06_O1() {
        Element child = new Element("i");
        Element root = new Element("div").appendChild(child);
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) {
                if (node == child) {
                    Element u = new Element("u");
                    child.replaceWith(u);
                }
            }
            public void tail(Node node, int depth) { }
        };
        NodeTraversor.traverse(visitor, root);
        Node first = root.childNode(0);
        assertEquals("u", first.nodeName());
    }

    @Test
    @DisplayName("deep tree with multiple siblings exercises sibling-null ascend loop and tail on last root")
    public void test_TC07_O1() {
        Element c1 = new Element("a");
        Element c2 = new Element("b");
        Element root = new Element("div").appendChild(c1).appendChild(c2);
        List<String> head = new ArrayList<>();
        List<String> tail = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) { head.add(node.nodeName() + ":" + depth); }
            public void tail(Node node, int depth) { tail.add(node.nodeName() + ":" + depth); }
        };
        NodeTraversor.traverse(visitor, root);
        assertEquals(List.of("div:0", "a:1", "b:1"), head);
        assertEquals(List.of("a:1", "b:1", "div:0"), tail);
    }

    @Test
    @DisplayName("throws IllegalArgumentException when NodeVisitor is null for Elements overload")
    public void test_TC08_O2() {
        NodeVisitor visitor = null;
        Elements elements = new Elements(new Element("div"));
        assertThrows(IllegalArgumentException.class, () -> NodeTraversor.traverse(visitor, elements));
    }

    @Test
    @DisplayName("throws IllegalArgumentException when Elements is null for Elements overload")
    public void test_TC09_O2() {
        NodeVisitor visitor = (n, d) -> { };
        Elements elements = null;
        assertThrows(IllegalArgumentException.class, () -> NodeTraversor.traverse(visitor, elements));
    }

    @Test
    @DisplayName("empty Elements iterator results in no traversal")
    public void test_TC10_O2() {
        Elements elements = new Elements();
        List<String> head = new ArrayList<>();
        List<String> tail = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) { head.add(node.nodeName() + ":" + depth); }
            public void tail(Node node, int depth) { tail.add(node.nodeName() + ":" + depth); }
        };
        NodeTraversor.traverse(visitor, elements);
        assertTrue(head.isEmpty());
        assertTrue(tail.isEmpty());
    }

    @Test
    @DisplayName("multiple Elements invokes traverse per element and stops none")
    public void test_TC11_O2() {
        Element e1 = new Element("p");
        Element e2 = new Element("span");
        Elements elements = new Elements(e1, e2);
        List<String> head = new ArrayList<>();
        List<String> tail = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) { head.add(node.nodeName() + ":" + depth); }
            public void tail(Node node, int depth) { tail.add(node.nodeName() + ":" + depth); }
        };
        NodeTraversor.traverse(visitor, elements);
        assertTrue(head.contains("p:0") && head.contains("span:0"));
        assertTrue(tail.contains("p:0") && tail.contains("span:0"));
    }
}