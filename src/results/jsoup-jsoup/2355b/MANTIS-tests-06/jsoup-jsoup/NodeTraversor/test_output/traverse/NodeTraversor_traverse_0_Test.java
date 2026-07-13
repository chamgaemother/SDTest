package org.jsoup.select;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.helper.Validate;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class NodeTraversor_traverse_0_Test {

    @Test
    @DisplayName("visitor null triggers IllegalArgumentException at entry (Validate.notNull(visitor))")
    public void test_TC01_O1() {
        NodeVisitor visitor = null;
        Node root = new Element("div");
        assertThrows(IllegalArgumentException.class, () -> {
            org.jsoup.select.NodeTraversor.traverse(visitor, root);
        });
    }

    @Test
    @DisplayName("root null triggers IllegalArgumentException at second Validate.notNull(root)")
    public void test_TC02_O1() {
        NodeVisitor visitor = (n, d) -> {};
        Node root = null;
        assertThrows(IllegalArgumentException.class, () -> {
            org.jsoup.select.NodeTraversor.traverse(visitor, root);
        });
    }

    @Test
    @DisplayName("single-node tree invokes head and tail once without descent (no children)")
    public void test_TC03_O1() {
        List<String> events = new ArrayList<>();
        NodeVisitor visitor = (n, d) -> events.add("head" + d);
        visitor = visitor.andThen((n, d) -> events.add("tail" + d));
        Node root = new TextNode("text"); // Changed constructor to match new signature
        org.jsoup.select.NodeTraversor.traverse(visitor, root);
        assertEquals(List.of("head0", "tail0"), events);
    }

    @Test
    @DisplayName("two-level tree descends into child and ascends back with correct depths")
    public void test_TC04_O1() {
        List<String> events = new ArrayList<>();
        NodeVisitor visitor = (n, d) -> events.add("head" + d);
        visitor = visitor.andThen((n, d) -> events.add("tail" + d));
        Element parent = new Element("div");
        parent.appendChild(new TextNode("child")); // Changed constructor to match new signature
        org.jsoup.select.NodeTraversor.traverse(visitor, parent);
        assertEquals(List.of("head0", "head1", "tail1", "tail0"), events);
    }

    @Test
    @DisplayName("sibling iteration: three siblings at root level without children")
    public void test_TC05_O1() {
        List<String> events = new ArrayList<>();
        NodeVisitor visitor = (n, d) -> events.add(n.outerHtml());
        Element root = new Element("root");
        root.appendChild(new Element("a"));
        root.appendChild(new Element("b"));
        root.appendChild(new Element("c"));
        org.jsoup.select.NodeTraversor.traverse(visitor, root.childNode(0));
        assertEquals(6, events.size());
    }

    @Test
    @DisplayName("node removed in head triggers remove branch and no tail for removed node")
    public void test_TC06_O1() {
        NodeVisitor visitor = (n, d) -> n.remove();
        Element root = new Element("div");
        root.appendChild(new TextNode("t")); // Changed constructor to match new signature
        org.jsoup.select.NodeTraversor.traverse(visitor, root.childNode(0));
        assertEquals(0, root.childNodeSize());
    }

    @Test
    @DisplayName("node replaced in head triggers replace branch and continues traversal on new node")
    public void test_TC07_O1() {
        NodeVisitor visitor = (n, d) -> n.replaceWith(new TextNode("x")); // Changed constructor to match new signature
        Element root = new Element("div");
        root.appendChild(new TextNode("orig")); // Changed constructor to match new signature
        org.jsoup.select.NodeTraversor.traverse(visitor, root.childNode(0));
        assertEquals("x", root.childNode(0).outerHtml());
    }

    @Test
    @DisplayName("deep tree with depth>1 triggers ancestor tail loop branch (node.nextSibling null && depth>0)")
    public void test_TC08_O1() {
        List<String> records = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node n, int d) { records.add("head" + d); }
            public void tail(Node n, int d) { records.add("tail" + d); }
        };
        Element root = Parser.parseFragment("<a><b><c/></b></a>", new Parser.Settings()).get(0); // Provided valid second argument
        org.jsoup.select.NodeTraversor.traverse(visitor, root);
        assertEquals(List.of("head0", "head1", "head2", "tail2", "tail1", "tail0"), records);
    }

    @Test
    @DisplayName("elements null in overload triggers IllegalArgumentException")
    public void test_TC09_O2() {
        NodeVisitor visitor = (n, d) -> {};
        Elements els = null;
        assertThrows(IllegalArgumentException.class, () -> {
            org.jsoup.select.NodeTraversor.traverse(visitor, els);
        });
    }

    @Test
    @DisplayName("empty Elements list results in no traversal calls")
    public void test_TC10_O2() {
        AtomicInteger count = new AtomicInteger();
        NodeVisitor visitor = (n, d) -> count.incrementAndGet();
        Elements els = new Elements();
        org.jsoup.select.NodeTraversor.traverse(visitor, els);
        assertEquals(0, count.get());
    }

    @Test
    @DisplayName("multiple Elements list invokes traverse for each Element")
    public void test_TC11_O2() {
        AtomicInteger count = new AtomicInteger();
        NodeVisitor visitor = (n, d) -> count.incrementAndGet();
        Element a = new Element("a");
        Element b = new Element("b");
        Elements els = new Elements(a, b);
        org.jsoup.select.NodeTraversor.traverse(visitor, els);
        assertEquals(2, count.get());
    }
}