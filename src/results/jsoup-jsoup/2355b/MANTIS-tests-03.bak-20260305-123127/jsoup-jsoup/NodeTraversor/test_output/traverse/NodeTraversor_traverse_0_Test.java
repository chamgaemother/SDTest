package org.jsoup.select;

import org.jsoup.helper.Validate;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeFilter;
import org.jsoup.select.NodeFilter.FilterResult;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NodeTraversor_traverse_0_Test {

    @Test
    @DisplayName("TC01_O1 throws IllegalArgumentException when visitor is null (Validate.notNull visitor)")
    public void test_TC01_O1() {
        NodeVisitor visitor = null;
        Element root = new Element("div");
        assertThrows(IllegalArgumentException.class,
                () -> NodeTraversor.traverse(visitor, (Node) root)); // Fixed type casting
    }

    @Test
    @DisplayName("TC02_O1 throws IllegalArgumentException when root is null (Validate.notNull root)")
    public void test_TC02_O1() {
        NodeVisitor visitor = (n, d) -> {};
        Node root = null;
        assertThrows(IllegalArgumentException.class,
                () -> NodeTraversor.traverse(visitor, root));
    }

    @Test
    @DisplayName("TC03_O1 single node with no children invokes head and tail exactly once")
    public void test_TC03_O1() {
        Element root = new Element("p");
        List<String> heads = new ArrayList<>();
        List<String> tails = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node n, int d) { heads.add(n.nodeName() + ":" + d); }
            public void tail(Node n, int d) { tails.add(n.nodeName() + ":" + d); }
        };
        NodeTraversor.traverse(visitor, (Node) root); // Fixed type casting
        assertEquals(List.of("p:0"), heads);
        assertEquals(List.of("p:0"), tails);
    }

    @Test
    @DisplayName("TC04_O1 node with one child descends once then ascends to root")
    public void test_TC04_O1() {
        Element root = new Element("div");
        Element child = new Element("span");
        root.appendChild(child);
        List<String> heads = new ArrayList<>();
        List<String> tails = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node n, int d) { heads.add(n.nodeName() + ":" + d); }
            public void tail(Node n, int d) { tails.add(n.nodeName() + ":" + d); }
        };
        NodeTraversor.traverse(visitor, (Node) root); // Fixed type casting
        assertEquals(List.of("div:0", "span:1"), heads);
        assertEquals(List.of("span:1", "div:0"), tails);
    }

    @Test
    @DisplayName("TC05_O1 node with two siblings ascends multiple times after child-level traversal")
    public void test_TC05_O1() {
        Element parent = new Element("ul");
        Element a = new Element("li");
        Element b = new Element("li");
        parent.appendChild(a);
        parent.appendChild(b);
        List<String> order = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node n, int d) { order.add(n.nodeName() + d); }
            public void tail(Node n, int d) { /* no-op */ }
        };
        NodeTraversor.traverse(visitor, (Node) parent); // Fixed type casting
        assertEquals(List.of("ul0", "li1", "li1", "ul0"), order);
    }

    @Test
    @DisplayName("TC06_O1 visitor.head removes node triggers 'removed' branch then continues at next sibling")
    public void test_TC06_O1() {
        Element root = new Element("div");
        Element c = new Element("span");
        root.appendChild(c);
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node n, int d) { n.remove(); }
            public void tail(Node n, int d) { /* no-op */ }
        };
        NodeTraversor.traverse(visitor, (Node) root); // Fixed type casting
        assertEquals(0, root.childNodeSize());
    }

    @Test
    @DisplayName("TC07_O1 visitor.head replaces node triggers 'replaced' branch and continues traversal")
    public void test_TC07_O1() {
        Element root = new Element("div");
        Element child = new Element("p");
        root.appendChild(child);
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node n, int d) {
                n.replaceWith(new Element(n.nodeName()));
            }
            public void tail(Node n, int d) { /* no-op */ }
        };
        NodeTraversor.traverse(visitor, (Node) root); // Fixed type casting
        assertEquals("p", root.childNode(0).nodeName());
    }

    @Test
    @DisplayName("TC08_O1 deep tree of depth 3 descends and ascends correctly (loop-N for depth)")
    public void test_TC08_O1() {
        Element root = new Element("root");
        Element c1 = new Element("n1");
        Element c2 = new Element("n2");
        Element c3 = new Element("n3");
        root.appendChild(c1);
        c1.appendChild(c2);
        c2.appendChild(c3);
        class CountingVisitor implements NodeVisitor {
            int count = 0;
            public void head(Node n, int d) { count++; }
            public void tail(Node n, int d) { count++; }
        }
        CountingVisitor visitor = new CountingVisitor();
        NodeTraversor.traverse(visitor, (Node) root); // Fixed type casting
        assertEquals(7, visitor.count);
    }

    @Test
    @DisplayName("TC09_O1 loop breaks at root tail when node equals root to exit (returns without exception)")
    public void test_TC09_O1() {
        Element root = new Element("div");
        NodeVisitor visitor = (n, d) -> {};
        assertDoesNotThrow(() -> NodeTraversor.traverse(visitor, (Node) root)); // Fixed type casting
    }

    @Test
    @DisplayName("TC10_O1 assertion enabled and node unexpectedly null triggers AssertionError")
    public void test_TC10_O1() {
        Element root = new Element("div");
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node n, int d) { n.replaceWith(null); }
            public void tail(Node n, int d) { /* no-op */ }
        };
        assertThrows(AssertionError.class, () -> {
            boolean assertsEnabled = false;
            assert assertsEnabled = true;
            if (!assertsEnabled) throw new IllegalStateException("Assertions must be enabled");
            NodeTraversor.traverse(visitor, (Node) root); // Fixed type casting
        });
    }

    @Test
    @DisplayName("TC11_O2 Elements overload with empty elements returns immediately (no iteration)")
    public void test_TC11_O2() {
        NodeVisitor visitor = (n, d) -> {};
        Elements elements = new Elements();
        assertDoesNotThrow(() -> NodeTraversor.traverse(visitor, elements)); // No change needed
        assertTrue(elements.isEmpty());
    }

    @Test
    @DisplayName("TC12_O2 Elements overload with one element invokes single Node overload")
    public void test_TC12_O2() {
        Element el = new Element("div");
        Elements elements = new Elements(el);
        List<String> heads = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node n, int d) { heads.add(n.nodeName() + ":" + d); }
            public void tail(Node n, int d) { /* no-op */ }
        };
        NodeTraversor.traverse(visitor, elements); // No change needed
        assertEquals(List.of("div:0"), heads);
    }

    @Test
    @DisplayName("TC13_O2 Elements overload stops iteration on STOP filter in nested Node overload")
    public void test_TC13_O2() {
        List<String> visited = new ArrayList<>();
        NodeFilter filter = new NodeFilter() {
            public FilterResult head(Node n, int d) {
                visited.add(n.nodeName() + ":" + d);
                return d == 0 ? FilterResult.STOP : FilterResult.CONTINUE;
            }
            public FilterResult tail(Node n, int d) {
                visited.add("tail-" + n.nodeName() + ":" + d);
                return FilterResult.CONTINUE;
            }
        };
        Elements elements = new Elements(new Element("a"), new Element("b"));
        NodeTraversor.filter(filter, elements); // No change needed
        assertEquals(List.of("a:0"), visited);
    }
}