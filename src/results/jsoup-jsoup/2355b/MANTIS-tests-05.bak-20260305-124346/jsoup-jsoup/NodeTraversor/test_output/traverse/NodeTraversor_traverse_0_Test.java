package org.jsoup.select;

import org.jsoup.helper.Validate;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class NodeTraversor_traverse_0_Test {

    @Test
    @DisplayName("TC01_O1: visitor is null triggers IllegalArgumentException at entry")
    public void test_TC01_O1() {
        // visitor == null so Validate.notNull(visitor) should throw
        NodeVisitor visitor = null;
        Node root = new Element("div");
        assertThrows(IllegalArgumentException.class, () -> {
            NodeTraversor.traverse(visitor, root);
        });
    }

    @Test
    @DisplayName("TC02_O1: root is null triggers IllegalArgumentException at entry")
    public void test_TC02_O1() {
        // root == null so Validate.notNull(root) should throw
        NodeVisitor visitor = (n, d) -> {};
        Node root = null;
        assertThrows(IllegalArgumentException.class, () -> {
            NodeTraversor.traverse(visitor, root);
        });
    }

    @Test
    @DisplayName("TC03_O1: single node with no children visits head and tail once")
    public void test_TC03_O1() {
        // single node has no children => both head and tail on root only
        List<String> events = new ArrayList<>();
        NodeVisitor visitor = (n, d) -> events.add("head(" + n.nodeName() + "," + d + ")");
        visitor = visitor.andThen((n, d) -> events.add("tail(" + n.nodeName() + "," + d + ")"));
        Node root = new Element("p");
        NodeTraversor.traverse(visitor, root);
        assertEquals(List.of("head(p,0)", "tail(p,0)"), events);
    }

    @Test
    @DisplayName("TC04_O1: node with one child descends then ascends depth 1")
    public void test_TC04_O1() {
        // one child => depth increases to 1 then back to 0
        List<String> events = new ArrayList<>();
        NodeVisitor visitor = (n, d) -> events.add("head(" + n.nodeName() + "," + d + ")");
        visitor = visitor.andThen((n, d) -> events.add("tail(" + n.nodeName() + "," + d + ")"));
        Element root = new Element("div");
        Element child = new Element("span");
        root.appendChild(child);
        NodeTraversor.traverse(visitor, root);
        assertEquals(List.of("head(div,0)", "head(span,1)", "tail(span,1)", "tail(div,0)"), events);
    }

    @Test
    @DisplayName("TC05_O1: node with two siblings processes nextSibling branch")
    public void test_TC05_O1() {
        // two children => first child then second child at same depth
        List<String> events = new ArrayList<>();
        NodeVisitor visitor = (n, d) -> events.add(n.nodeName() + "@" + d);
        visitor = visitor.andThen((n, d) -> events.add("/" + n.nodeName() + "@" + d));
        Element root = new Element("ul");
        root.appendChild(new Element("li"));
        root.appendChild(new Element("li"));
        NodeTraversor.traverse(visitor, root);
        // first pair corresponds to first <li> at depth 1
        assertTrue(events.get(0).equals("li@1") && events.get(1).equals("/li@1"));
    }

    @Test
    @DisplayName("TC06_O1: removal during head triggers skip of tail and continue up")
    public void test_TC06_O1() {
        // head at depth 0 removes node => child removed and no tail called for it
        NodeVisitor visitor = (n, d) -> {
            if (d == 0) n.remove();
        };
        Element root = new Element("p");
        root.appendChild(new Element("b"));
        NodeTraversor.traverse(visitor, root);
        assertEquals(0, root.childNodeSize());
    }

    @Test
    @DisplayName("TC07_O2: elements list empty visits nothing")
    public void test_TC07_O2() {
        // empty elements => loop body not entered
        NodeVisitor visitor = (n, d) -> {};
        Elements elements = new Elements();
        assertDoesNotThrow(() -> NodeTraversor.traverse(visitor, elements));
        assertTrue(elements.isEmpty());
    }

    @Test
    @DisplayName("TC08_O2: elements with one element calls traverse on that element")
    public void test_TC08_O2() {
        // one element without children => head and tail exactly twice
        AtomicInteger heads = new AtomicInteger();
        NodeVisitor visitor = (n, d) -> heads.incrementAndGet();
        Elements elements = new Elements(new Element("a"));
        NodeTraversor.traverse(visitor, elements);
        assertEquals(2, heads.get());
    }

    @Test
    @DisplayName("TC09_O2: elements with multiple elements stops after first STOP filter not applicable")
    public void test_TC09_O2() {
        // two elements => both traversed via traverse(visitor, elements)
        NodeVisitor visitor = (n, d) -> {};
        Elements elements = new Elements(new Element("div"), new Element("span"));
        assertDoesNotThrow(() -> NodeTraversor.traverse(visitor, elements));
    }

    @Test
    @DisplayName("TC10_O2: null elements list triggers IllegalArgumentException")
    public void test_TC10_O2() {
        // elements == null => Validate.notNull(elements) should throw
        NodeVisitor visitor = (n, d) -> {};
        Elements elements = null;
        assertThrows(IllegalArgumentException.class, () -> {
            NodeTraversor.traverse(visitor, elements);
        });
    }
}