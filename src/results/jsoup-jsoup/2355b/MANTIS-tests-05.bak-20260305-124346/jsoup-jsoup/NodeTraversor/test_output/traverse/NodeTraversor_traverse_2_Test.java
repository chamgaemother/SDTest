package org.jsoup.select;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
public class NodeTraversor_traverse_2_Test {

    @Test
    @DisplayName("visitor.head replaces a child node on direct traverse(NodeVisitor,Node) triggers replacement and B6→B7→B8 path")
    public void test_TC15() {
        // GIVEN root with one child c1 so B11->B12 descend, but head replaces c1 to c2 triggers replacement branch
        Element root = new Element("div");
        Element c1 = new Element("span");
        root.appendChild(c1);
        Element c2 = new Element("em");
        List<String> events = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node n, int d) {
                if (n.equals(c1)) {
                    // replace c1 with c2 to take B7->B8
                    n.replaceWith(c2);
                }
                if (n.equals(c2)) {
                    events.add("head(" + n.nodeName() + "," + d + ")");
                }
            }

            @Override
            public void tail(Node n, int d) {
                if (n.equals(c2) || n.equals(root)) {
                    events.add("tail(" + n.nodeName() + "," + d + ")");
                }
            }
        };
        // WHEN
        NodeTraversor.traverse(visitor, root);
        // THEN c2 should be visited in head and tail, and root tail
        assertTrue(events.contains("head(em,1)"), "Expected head(em,1)");
        assertTrue(events.contains("tail(em,1)"), "Expected tail(em,1)");
        assertTrue(events.contains("tail(div,0)"), "Expected tail(div,0)");
    }

    @Test
    @DisplayName("visitor.head removes a middle child on direct traverse to hit removal with non-null nextSibling (B6→B7→B9→B10)")
    public void test_TC16() {
        // GIVEN root with two children c1,c2 so removal of c1 triggers B7->B9->B10 (nextSibling non-null)
        Element root = new Element("ul");
        Element c1 = new Element("li");
        Element c2 = new Element("li");
        root.appendChild(c1);
        root.appendChild(c2);
        List<String> events = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node n, int d) {
                if (n.equals(c1)) {
                    // remove c1 to take B7->B9->B10
                    n.remove();
                }
                events.add("head(" + n.nodeName() + "," + d + ")");
            }

            @Override
            public void tail(Node n, int d) {
                events.add("tail(" + n.nodeName() + "," + d + ")");
            }
        };
        // WHEN
        NodeTraversor.traverse(visitor, root);
        // THEN no tail on removed c1 at depth1 and traversal continues at c2
        // Ensure events for c1 do not include any tail marker
        boolean removedTail = events.stream().anyMatch(e -> e.equals("tail(li,1)") && e.contains("li,1"));
        assertFalse(removedTail, "Removed node c1 should not produce a tail event");
        // And c2 is visited normally at depth1
        assertTrue(events.contains("head(li,1)"), "Expected head(li,1) for c2");
    }

    @Test
    @DisplayName("deep nested siblings covers ascend without prune when depth>0 and nextSibling==null leading to B16→B17→B18→B20")
    public void test_TC17() {
        // GIVEN nested structure root->b->{c,d}; c at depth2 with a sibling so ascend loop covers tail then sibling
        Element root = new Element("div");
        Element b = new Element("b");
        root.appendChild(b);
        Element c = new Element("c");
        Element d = new Element("d");
        b.appendChild(c);
        b.appendChild(d);
        List<String> events = new ArrayList<>();
        // visitor that records head and tail events
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
        NodeTraversor.traverse(visitor, root);
        // THEN c at depth2 should have both head and tail entries
        assertTrue(events.contains("c@2"), "Expected head event for c at depth2");
        assertTrue(events.contains("/c@2"), "Expected tail event for c at depth2");
    }
}