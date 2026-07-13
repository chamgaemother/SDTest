package org.jsoup.select;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NodeTraversor_traverse_2_Test {

    @Test
    @DisplayName("traverse(visitor,Elements) throws IllegalArgumentException when visitor is null")
    public void test_TC11() {
        // GIVEN a null visitor and a non-null Elements to trigger Validate.notNull(visitor)
        NodeVisitor visitor = null;
        Elements elements = new Elements();
        // WHEN & THEN IllegalArgumentException is thrown immediately on null visitor
        assertThrows(IllegalArgumentException.class, () -> NodeTraversor.traverse(visitor, elements));
    }

    @Test
    @DisplayName("traverse(visitor,Elements) throws IllegalArgumentException when elements is null")
    public void test_TC12() {
        // GIVEN a non-null visitor and a null Elements to trigger Validate.notNull(elements)
        NodeVisitor visitor = (n, d) -> { /* no-op */ };
        Elements elements = null;
        // WHEN & THEN IllegalArgumentException is thrown immediately on null elements
        assertThrows(IllegalArgumentException.class, () -> NodeTraversor.traverse(visitor, elements));
    }

    @Test
    @DisplayName("removing the last sibling in head triggers the ‘removed and next=null’ branch")
    public void test_TC13() {
        // GIVEN a root with two children "a" and "b"; visitor removes "b" in head, so parent!=null, hasParent==false after removal, origSize!=size and nextSibling()==null
        Element root = new Element("div");
        Element a = new Element("a");
        Element b = new Element("b");
        root.appendChild(a);
        root.appendChild(b);
        List<String> events = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node n, int d) {
                events.add("H:" + n.nodeName());
                if (n.nodeName().equals("b")) {
                    n.remove(); // remove last sibling to force origSize!=size and nextSibling()==null
                }
            }
            @Override
            public void tail(Node n, int d) {
                events.add("T:" + n.nodeName());
            }
        };
        // WHEN
        NodeTraversor.traverse(visitor, root);
        // THEN_H:b and no T:b, but a is fully visited
        assertTrue(events.contains("H:b"), "head for b should be recorded");
        assertFalse(events.contains("T:b"), "tail for removed b must be skipped");
        assertTrue(events.contains("H:a") && events.contains("T:a"), "a should be both head and tail visited");
    }

    @Test
    @DisplayName("root removal in head skips parent-based branches and completes traversal")
    public void test_TC14() {
        // GIVEN a single-node root; in head the node is removed (parentNode()==null) to skip parent-based branches
        Element root = new Element("div");
        List<String> events = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node n, int d) {
                events.add("H:" + n.nodeName() + ":" + d);
                n.remove(); // remove root, so parentNode() remains null
            }
            @Override
            public void tail(Node n, int d) {
                events.add("T:" + n.nodeName() + ":" + d);
            }
        };
        // WHEN
        NodeTraversor.traverse(visitor, root);
        // THEN exactly one head and one tail on root at depth 0
        assertEquals(Arrays.asList("H:div:0", "T:div:0"), events);
    }
}