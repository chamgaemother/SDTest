package org.jsoup.select;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class NodeTraversor_traverse_0_Test {

    @Test
    @DisplayName("TC01_O1: throws IllegalArgumentException when visitor is null (Validate.notNull(visitor))")
    public void test_TC01_O1() {
        Node root = new Element("div");
        // visitor is null to trigger Validate.notNull(visitor)
        assertThrows(IllegalArgumentException.class, () -> {
            NodeTraversor.traverse((NodeVisitor) null, root);
        });
    }

    @Test
    @DisplayName("TC02_O1: throws IllegalArgumentException when root is null (Validate.notNull(root))")
    public void test_TC02_O1() {
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) {}
            public void tail(Node node, int depth) {}
        };
        // root is null to trigger Validate.notNull(root)
        assertThrows(IllegalArgumentException.class, () -> {
            NodeTraversor.traverse(visitor, null);
        });
    }

    @Test
    @DisplayName("TC03_O1: single-node tree invokes head and tail exactly once (no children)")
    public void test_TC03_O1() {
        TextNode root = new TextNode("text");
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) {
                calls.add("head(" + ((TextNode)node).text() + "," + depth + ");");
            }
            public void tail(Node node, int depth) {
                calls.add("tail(" + ((TextNode)node).text() + "," + depth + ");");
            }
        };
        // depth=0, no children => one head and one tail
        NodeTraversor.traverse(visitor, root);
        assertEquals(2, calls.size());
        assertEquals("head(text,0)", calls.get(0));
        assertEquals("tail(text,0)", calls.get(1));
    }

    @Test
    @DisplayName("TC04_O1: two-level tree descends then ascends (one child)")
    public void test_TC04_O1() {
        Element root = new Element("div");
        TextNode child = new TextNode("c");
        root.appendChild(child);
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) {
                String name = (node instanceof Element) ? ((Element)node).tagName() : ((TextNode)node).text();
                calls.add("head(" + name + "," + depth + ");");
            }
            public void tail(Node node, int depth) {
                String name = (node instanceof Element) ? ((Element)node).tagName() : ((TextNode)node).text();
                calls.add("tail(" + name + "," + depth + ");");
            }
        };
        // one child => head(root,0), head(child,1), tail(child,1), tail(root,0)
        NodeTraversor.traverse(visitor, root);
        assertEquals(List.of(
            "head(div,0)",
            "head(c,1)",
            "tail(c,1)",
            "tail(div,0)"
        ), calls);
    }

    @Test
    @DisplayName("TC05_O1: tree with two siblings tests sibling next logic and multiple tail/ascend")
    public void test_TC05_O1() {
        Element root = new Element("ul");
        Element li1 = new Element("li");
        Element li2 = new Element("li");
        root.appendChild(li1);
        root.appendChild(li2);
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) {
                calls.add("head(" + ((Element)node).tagName() + "," + depth + ");");
            }
            public void tail(Node node, int depth) {
                calls.add("tail(" + ((Element)node).tagName() + "," + depth + ");");
            }
        };
        // two siblings => visit each then tail root
        NodeTraversor.traverse(visitor, root);
        assertEquals(List.of(
            "head(ul,0)",
            "head(li,1)", "tail(li,1)",
            "head(li,1)", "tail(li,1)",
            "tail(ul,0)"
        ), calls);
    }

    @Test
    @DisplayName("TC06_O1: node removed in head triggers remove branch (origSize!=childNodeSize)")
    public void test_TC06_O1() {
        Element root = new Element("div");
        TextNode x = new TextNode("x");
        root.appendChild(x);
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) {
                if (node instanceof TextNode) {
                    node.remove(); // remove in head
                }
                calls.add("head(" + (node instanceof Element ? ((Element)node).tagName() : ((TextNode)node).text()) + "," + depth + ");");
            }
            public void tail(Node node, int depth) {
                calls.add("tail(" + (node instanceof Element ? ((Element)node).tagName() : ((TextNode)node).text()) + "," + depth + ");");
            }
        };
        // removal branch: head(root,0), head(x,1 removed), then tail(root,0)
        NodeTraversor.traverse(visitor, root);
        assertTrue(calls.contains("head(div,0)"));
        assertTrue(calls.contains("head(x,1)"));
        assertFalse(calls.stream().anyMatch(s -> s.startsWith("tail(x"));
        assertTrue(calls.contains("tail(div,0)"));
    }

    @Test
    @DisplayName("TC07_O1: node replaced in head triggers replace branch (origSize==childNodeSize)")
    public void test_TC07_O1() {
        Element root = new Element("div");
        TextNode oldChild = new TextNode("old");
        root.appendChild(oldChild);
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) {
                if (node instanceof TextNode && ((TextNode) node).text().equals("old")) {
                    TextNode replacement = new TextNode("new");
                    node.replaceWith(replacement);
                }
                String name = (node instanceof Element) ? ((Element)node).tagName() : ((TextNode)node).text();
                calls.add("head(" + name + "," + depth + ");");
            }
            public void tail(Node node, int depth) {
                String name = (node instanceof Element) ? ((Element)node).tagName() : ((TextNode)node).text();
                calls.add("tail(" + name + "," + depth + ");");
            }
        };
        // replace branch: head(root,0), head(old->new,1), tail(new,1), tail(root,0)
        NodeTraversor.traverse(visitor, root);
        assertTrue(calls.contains("head(div,0)"));
        assertTrue(calls.contains("head(new,1)"));
        assertTrue(calls.contains("tail(new,1)"));
        assertTrue(calls.contains("tail(div,0)"));
    }

    @Test
    @DisplayName("TC08_O2: empty Elements list yields no traversal")
    public void test_TC08_O2() {
        Elements els = new Elements();
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) { calls.add("head"); }
            public void tail(Node node, int depth) { calls.add("tail"); }
        };
        // empty list => no head/tail calls
        NodeTraversor.traverse(visitor, new Element("div"));
        assertTrue(calls.isEmpty());
    }

    @Test
    @DisplayName("TC09_O2: single Element in Elements calls traverse once (delegation)")
    public void test_TC09_O2() {
        TextNode el = new TextNode("t");
        Elements els = new Elements(new Element("div").appendChild(el));
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) {
                calls.add("head(" + ((TextNode)node).text() + "," + depth + ");");
            }
            public void tail(Node node, int depth) {
                calls.add("tail(" + ((TextNode)node).text() + "," + depth + ");");
            }
        };
        // one element => head & tail of that element once
        NodeTraversor.traverse(visitor, els);
        assertEquals(List.of("head(t,0)", "tail(t,0)"), calls);
    }

    @Test
    @DisplayName("TC10_O2: multiple Elements stops only after iterating all elements")
    public void test_TC10_O2() {
        TextNode a = new TextNode("a");
        TextNode b = new TextNode("b");
        Elements els = new Elements(new Element("div").appendChild(a).appendChild(b));
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) {
                calls.add("head(" + ((TextNode)node).text() + "," + depth + ");");
            }
            public void tail(Node node, int depth) {
                calls.add("tail(" + ((TextNode)node).text() + "," + depth + ");
            }
        };
        // two elements => each head/tail in order a then b
        NodeTraversor.traverse(visitor, els);
        assertEquals(List.of(
            "head(a,0)", "tail(a,0)",
            "head(b,0)", "tail(b,0)"
        ), calls);
    }
}