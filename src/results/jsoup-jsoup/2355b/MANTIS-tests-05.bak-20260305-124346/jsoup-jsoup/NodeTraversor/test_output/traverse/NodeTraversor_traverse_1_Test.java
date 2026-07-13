package org.jsoup.select;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NodeTraversor_traverse_1_Test {

    @Test
    @DisplayName("visitor.head replaces a child node (origSize == parent.childNodeSize) triggers replace branch")
    public void test_TC11() {
        // GIVEN root with one child c1; visitor replaces c1 with c2 in head
        Element root = new Element("div");
        Element c1 = new Element("span");
        root.appendChild(c1);
        Element c2 = new Element("em");
        List<String> events = new ArrayList<>();

        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node n, int depth) {
                // trigger replacement branch: origSize == parent.childNodeSize
                if (n.equals(c1)) {
                    n.replaceWith(c2);
                }
                // record events after replacement
                if (n.equals(c2)) {
                    events.add(n.nodeName() + "@" + depth);
                }
            }
            @Override
            public void tail(Node n, int depth) {
                // record tail of replaced and root only
                if (n.equals(c2) || n.equals(root)) {
                    events.add("/" + n.nodeName() + "@" + depth);
                }
            }
        };

        // WHEN
        NodeTraversor.traverse(visitor, root.children()); // Updated method call

        // THEN
        // Verify replacement visited c2 at depth 1 head and tail, and then root tail
        assertTrue(events.contains("em@1"), "Expected head of replacement node em at depth 1");
        assertTrue(events.contains("/em@1"), "Expected tail of replacement node em at depth 1");
        assertTrue(events.contains("/div@0"), "Expected tail of root at depth 0");
        // Ensure original child c1 was not recorded
        assertFalse(events.stream().anyMatch(e -> e.startsWith("span@")), "Original node span should be replaced and not visited");
    }

    @Test
    @DisplayName("visitor.head removes a grandchild triggers removed-continue-up branch (origSize != parent.childNodeSize)")
    public void test_TC12() {
        // GIVEN root->li->gc; visitor removes gc in head to trigger removal branch
        Element root = new Element("ul");
        Element li = new Element("li");
        root.appendChild(li);
        Element gc = new Element("b");
        li.appendChild(gc);
        List<String> events = new ArrayList<>();

        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node n, int depth) {
                // trigger removal: origSize != parent.childNodeSize
                if (n.equals(gc)) {
                    n.remove();
                }
                // record all head events
                events.add(n.nodeName() + "@" + depth);
            }
            @Override
            public void tail(Node n, int depth) {
                // record all tail events
                events.add("/" + n.nodeName() + "@" + depth);
            }
        };

        // WHEN
        NodeTraversor.traverse(visitor, root.children()); // Updated method call

        // THEN
        // grandchild should never appear (neither head nor tail)
        assertFalse(events.stream().anyMatch(e -> e.startsWith("b@") || e.startsWith("/b@")),
                "Grandchild b should be removed before tail and not recorded");
        // parent li still visited head and tail at depth 1
        assertTrue(events.contains("li@1"), "Expected head of li at depth 1");
        assertTrue(events.contains("/li@1"), "Expected tail of li at depth 1");
    }

    @Test
    @DisplayName("deep tree with no siblings ascends through multiple tail calls")
    public void test_TC13() {
        // GIVEN a deep chain root->a->b->c with no siblings to cover ascend-loop
        Element root = new Element("d");
        Element a = new Element("a"); root.appendChild(a);
        Element b = new Element("b"); a.appendChild(b);
        Element c = new Element("c"); b.appendChild(c);
        List<String> events = new ArrayList<>();

        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node n, int depth) {
                // record head events
                events.add(n.nodeName() + "@" + depth);
            }
            @Override
            public void tail(Node n, int depth) {
                // record tail events
                events.add("/" + n.nodeName() + "@" + depth);
            }
        };

        // WHEN
        NodeTraversor.traverse(visitor, root.children()); // Updated method call

        // THEN
        // Expect heads in order d0,a1,b2,c3 then tails c3,b2,a1,d0
        List<String> expected = List.of(
                "d@0", "a@1", "b@2", "c@3",
                "/c@3", "/b@2", "/a@1", "/d@0"
        );
        assertEquals(expected, events, "Deep chain without siblings should ascend correctly with multiple tail calls");
    }

    @Test
    @DisplayName("visitor with no children (node.childNodeSize()==0) triggers direct tail and break at root==node")
    public void test_TC14() {
        // GIVEN root with no children triggers direct tail then exit
        Element root = new Element("p");
        List<String> events = new ArrayList<>();

        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node n, int depth) {
                events.add(n.nodeName() + "@" + depth);
            }
            @Override
            public void tail(Node n, int depth) {
                events.add("/" + n.nodeName() + "@" + depth);
            }
        };

        // WHEN
        NodeTraversor.traverse(visitor, root.children()); // Updated method call

        // THEN
        assertEquals(List.of("p@0", "/p@0"), events,
                "Single node with no children should have one head and one tail then exit");
    }
}