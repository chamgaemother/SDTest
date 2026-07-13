package org.jsoup.select;

import static org.junit.jupiter.api.Assertions.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class NodeTraversor_traverse_0_Test {

    @Test
    @DisplayName("TC01: traverse(null, validRoot) throws IllegalArgumentException when visitor is null")
    void test_TC01() {
        NodeVisitor visitor = null;
        Node root = new TextNode("text");
        // visitor null triggers Validate.notNull(visitor)
        assertThrows(IllegalArgumentException.class, () -> NodeTraversor.traverse(visitor, root));
    }

    @Test
    @DisplayName("TC02: traverse(validVisitor, null) throws IllegalArgumentException when root is null")
    void test_TC02() {
        NodeVisitor visitor = (n, d) -> {};
        Node root = null;
        // root null triggers Validate.notNull(root)
        assertThrows(IllegalArgumentException.class, () -> NodeTraversor.traverse(visitor, root));
    }

    @Test
    @DisplayName("TC03: traverse on single leaf node calls head and tail once then returns")
    void test_TC03() {
        List<String> calls = new ArrayList<>();
        TextNode root = new TextNode("leaf");
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node n, int d) { calls.add("head:" + n.getClass().getSimpleName() + ":" + d); }
            public void tail(Node n, int d) { calls.add("tail:" + n.getClass().getSimpleName() + ":" + d); }
        };
        // single node, no children => depth 0 head+tail exactly once
        NodeTraversor.traverse(visitor, (Node) root);
        assertEquals(List.of("head:TextNode:0", "tail:TextNode:0"), calls);
    }

    @Test
    @DisplayName("TC04: traverse on root with one child descends then ascends")
    void test_TC04() {
        List<String> calls = new ArrayList<>();
        Element root = new Element("div");
        root.appendChild(new TextNode("c"));
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node n, int d) { calls.add("head:" + n.nodeName() + ":" + d); }
            public void tail(Node n, int d) { calls.add("tail:" + n.nodeName() + ":" + d); }
        };
        // root has one child => descend into child at depth1 then ascend
        NodeTraversor.traverse(visitor, (Node) root);
        assertEquals(List.of("head:div:0","head:#text:1","tail:#text:1","tail:div:0"), calls);
    }

    @Test
    @DisplayName("TC05: traverse handles replacement in head: parent child size same triggers replace branch")
    void test_TC05() {
        Element root = new Element("p");
        root.appendChild(new TextNode("a"));
        root.appendChild(new TextNode("b"));
        List<String> visited = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node n, int d) {
                if (n.parentNode() != null) {
                    // replace with same-index text node
                    n.replaceWith(new TextNode("x"));
                }
                visited.add("h:" + n.outerHtml() + ":" + d);
            }
            public void tail(Node n, int d) {
                visited.add("t:" + n.outerHtml() + ":" + d);
            }
        };
        // replacement should not break traversal
        NodeTraversor.traverse(visitor, (Node) root);
        // expect 1 head/tail for root plus for each replaced child in order
        assertTrue(visited.size() >= 6, "Should visit root and both replaced children");
    }

    @Test
    @DisplayName("TC06: traverse handles removal in head: parent child size changes triggers removal branch")
    void test_TC06() {
        Element root = new Element("ul");
        Element li = new Element("li");
        li.text("text");
        root.appendChild(li);
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node n, int d) {
                // remove every node
                n.remove();
            }
            public void tail(Node n, int d) { fail("tail should not be called for removed node"); }
        };
        // removal means no tail on child, root.childNodeSize goes to 0
        NodeTraversor.traverse(visitor, (Node) root);
        assertEquals(0, root.childNodeSize());
    }

    @Test
    @DisplayName("TC07: traverse on sibling chain visits all siblings in order")
    void test_TC07() {
        Element root = new Element("div");
        root.appendChild(new TextNode("a"));
        root.appendChild(new TextNode("b"));
        List<String> seq = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node n, int d) { seq.add("h:" + n.outerHtml()); }
            public void tail(Node n, int d) { seq.add("t:" + n.outerHtml()); }
        };
        // two siblings under root at depth1 triggers sibling nextSibling branch
        NodeTraversor.traverse(visitor, (Node) root);
        // only text children visited
        assertEquals(List.of("h:a","t:a","h:b","t:b"), seq);
    }

    @Test
    @DisplayName("TC08: traverse on deeper tree with multiple descendant levels")
    void test_TC08() {
        Element root = Jsoup.parse("<div><span><em>deep</em></span></div>").body().child(0);
        List<Integer> depths = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node n, int d) { depths.add(d); }
            public void tail(Node n, int d) { depths.add(d); }
        };
        // tree depth 0->1->2 for head then 2->1->0 for tail
        NodeTraversor.traverse(visitor, (Node) root);
        assertEquals(List.of(0,1,2,2,1,0), depths);
    }

    @Test
    @DisplayName("TC09: traverse(elements) with empty list does nothing")
    void test_TC09() {
        Elements elems = new Elements();
        AtomicInteger count = new AtomicInteger();
        NodeVisitor visitor = (n,d) -> count.incrementAndGet();
        // empty list => traverse not invoked
        NodeTraversor.traverse(visitor, elems);
        assertEquals(0, count.get());
    }

    @Test
    @DisplayName("TC10: traverse(elements) with one element invokes child overload once")
    void test_TC10() {
        Element e = new Element("p");
        Elements elems = new Elements(e);
        AtomicInteger calls = new AtomicInteger();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node n, int d) { calls.incrementAndGet(); }
            public void tail(Node n, int d) { calls.incrementAndGet(); }
        };
        // single element yields two calls
        NodeTraversor.traverse(visitor, elems);
        assertEquals(2, calls.get());
    }

    @Test
    @DisplayName("TC11: traverse(elements) with multiple elements invokes child overload for each in order")
    void test_TC11() {
        Element a = new Element("a");
        Element b = new Element("b");
        Elements elems = new Elements(a, b);
        List<String> seq = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node n, int d) { seq.add(n.nodeName()); }
            public void tail(Node n, int d) { seq.add(n.nodeName() + ":tail"); }
        };
        // two elements => each head+tail
        NodeTraversor.traverse(visitor, elems);
        assertEquals(List.of("a","a:tail","b","b:tail"), seq);
    }

    @Test
    @DisplayName("TC12: traverse on node with multiple siblings and children covers both descend and ascend loops")
    void test_TC12() {
        Element root = Jsoup.parse(
            "<div><p>x</p><ul><li>1</li><li>2</li></ul></div>"
        ).body().child(0);
        List<String> log = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node n, int d) { log.add("h:" + n.nodeName() + ":" + d); }
            public void tail(Node n, int d) { log.add("t:" + n.nodeName() + ":" + d); }
        };
        // complex tree: p then ul->li
        NodeTraversor.traverse(visitor, (Node) root);
        // verify starts with root head, p head/tail, ul head, li head/tail, then second li head/tail, then ul tail, root tail
        List<String> expected = List.of(
            "h:div:0",
            "h:p:1","t:p:1",
            "h:ul:1",
            "h:li:2","t:li:2",
            "h:li:2","t:li:2",
            "t:ul:1",
            "t:div:0"
        );
        assertEquals(expected, log);
    }
}