package org.jsoup.select;

import org.jsoup.helper.Validate;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NodeTraversor_traverse_0_Test {

    @Test
    @DisplayName("throws IllegalArgumentException when visitor is null (Validate.notNull r0 == null)")
    public void test_TC01_O1() {
        // visitor null triggers Validate.notNull(visitor)
        Element root = new Element("div");
        assertThrows(IllegalArgumentException.class, () -> NodeTraversor.traverse((NodeVisitor) null, root));
    }

    @Test
    @DisplayName("throws IllegalArgumentException when root is null (Validate.notNull r1 == null)")
    public void test_TC02_O1() {
        // root null triggers Validate.notNull(root)
        NodeVisitor visitor = (node, depth) -> {};
        assertThrows(IllegalArgumentException.class, () -> NodeTraversor.traverse(visitor, (Node) null));
    }

    @Test
    @DisplayName("visits single root node with no children (no descend, immediate tail then return)")
    public void test_TC03_O1() {
        // single node has childNodeSize 0 -> B11->B12
        Element root = new Element("p");
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) { calls.add("head:" + node.nodeName() + ":" + depth); }
            public void tail(Node node, int depth) { calls.add("tail:" + node.nodeName() + ":" + depth); }
        };
        NodeTraversor.traverse(visitor, root);
        assertEquals(2, calls.size());
        assertEquals("head:p:0", calls.get(0));
        assertEquals("tail:p:0", calls.get(1));
    }

    @Test
    @DisplayName("descends one level: root with one child invokes head/tail in depth-first order")
    public void test_TC04_O1() {
        // root has one child, triggers descend path B11->B12 twice
        Element root = new Element("div");
        Element child = new Element("span");
        root.appendChild(child);
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) { calls.add("head:" + node.nodeName() + ":" + depth); }
            public void tail(Node node, int depth) { calls.add("tail:" + node.nodeName() + ":" + depth); }
        };
        NodeTraversor.traverse(visitor, root);
        assertEquals(List.of(
            "head:div:0",
            "head:span:1",
            "tail:span:1",
            "tail:div:0"
        ), calls);
    }

    @Test
    @DisplayName("handles multiple siblings: root with two children visits siblings sequentially")
    public void test_TC05_O1() {
        // root has two children -> sibling sequence
        Element root = new Element("ul");
        Element li1 = new Element("li");
        Element li2 = new Element("li");
        root.appendChild(li1);
        root.appendChild(li2);
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) { calls.add("head:" + node.nodeName() + ":" + depth); }
            public void tail(Node node, int depth) { calls.add("tail:" + node.nodeName() + ":" + depth); }
        };
        NodeTraversor.traverse(visitor, root);
        assertEquals(List.of(
            "head:ul:0",
            "head:li:1",
            "tail:li:1",
            "head:li:1",
            "tail:li:1",
            "tail:ul:0"
        ), calls);
    }

    @Test
    @DisplayName("replaces node in head: visitor.head replaces node content but keeps siblingIndex (replacement path)")
    public void test_TC06_O1() {
        // visitor.head on child invokes replaceWith -> origSize==childNodeSize so replacement path B7->B8
        Element root = new Element("div");
        Element child = new Element("span");
        root.appendChild(child);
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) {
                calls.add("head:" + node.nodeName() + ":" + depth);
                if (node == child) {
                    node.replaceWith(new Element("a"));
                }
            }
            public void tail(Node node, int depth) {
                calls.add("tail:" + node.nodeName() + ":" + depth);
            }
        };
        NodeTraversor.traverse(visitor, root);
        // Expect head on div, head on span, head on replacement 'a', tail on 'a', tail on div
        assertTrue(calls.contains("head:div:0"));
        assertTrue(calls.contains("head:span:1"));
        assertTrue(calls.contains("head:a:1"));
        assertTrue(calls.contains("tail:a:1"));
        assertTrue(calls.contains("tail:div:0"));
    }

    @Test
    @DisplayName("removes node in head: visitor.head removes node completely (removal path)")
    public void test_TC07_O1() {
        // visitor.head on child invokes remove -> origSize>childNodeSize so removal path B7->B9
        Element root = new Element("div");
        Element child = new Element("span");
        root.appendChild(child);
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) {
                calls.add("head:" + node.nodeName() + ":" + depth);
                if (node == child) node.remove();
            }
            public void tail(Node node, int depth) {
                calls.add("tail:" + node.nodeName() + ":" + depth);
            }
        };
        NodeTraversor.traverse(visitor, root);
        // head called on div and child, tail only on div
        assertTrue(calls.contains("head:div:0"));
        assertTrue(calls.contains("head:span:1"));
        assertFalse(calls.contains("tail:span:1"));
        assertTrue(calls.contains("tail:div:0"));
    }

    @Test
    @DisplayName("throws IllegalArgumentException when Element list is null (Validate.notNull elements == null)")
    public void test_TC08_O2() {
        // elements null triggers Validate.notNull(elements)
        NodeVisitor visitor = (n,d) -> {};
        assertThrows(IllegalArgumentException.class, () -> NodeTraversor.traverse(visitor, (Elements) null));
    }

    @Test
    @DisplayName("iterates zero elements when Elements is empty (for-loop 0 iterations)")
    public void test_TC09_O2() {
        // empty Elements -> no traverse on NodeVisitor
        Elements els = new Elements();
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) { calls.add("head"); }
            public void tail(Node node, int depth) { calls.add("tail"); }
        };
        NodeTraversor.traverse(visitor, els);
        assertTrue(calls.isEmpty());
    }

    @Test
    @DisplayName("iterates one element in list (for-loop 1 iteration)")
    public void test_TC10_O2() {
        // single element in Elements -> one traverse call
        Element el = new Element("p");
        Elements els = new Elements(el);
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) { calls.add("head:" + node.nodeName()); }
            public void tail(Node node, int depth) { calls.add("tail:" + node.nodeName()); }
        };
        NodeTraversor.traverse(visitor, els);
        assertEquals(2, calls.size()); // head and tail for that element
        assertEquals("head:p", calls.get(0));
        assertEquals("tail:p", calls.get(1));
    }

    @Test
    @DisplayName("iterates multiple elements in list (for-loop N>1 iterations)")
    public void test_TC11_O2() {
        // two elements in Elements -> two traverse calls in order
        Element e1 = new Element("a");
        Element e2 = new Element("b");
        Elements els = new Elements(e1, e2);
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) { calls.add("head:" + node.nodeName() + ":" + depth); }
            public void tail(Node node, int depth) { calls.add("tail:" + node.nodeName() + ":" + depth); }
        };
        NodeTraversor.traverse(visitor, els);
        // Expect head on e1 then head on e2 at depth0
        assertTrue(calls.indexOf("head:a:0") < calls.indexOf("head:b:0"));
        assertTrue(calls.contains("head:a:0") && calls.contains("head:b:0"));
    }
}