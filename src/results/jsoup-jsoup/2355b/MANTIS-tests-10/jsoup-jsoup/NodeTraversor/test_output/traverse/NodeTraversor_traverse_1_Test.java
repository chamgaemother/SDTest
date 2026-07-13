package org.jsoup.select;

import org.jsoup.helper.Validate;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class NodeTraversor_traverse_1_Test {

    /**
     * RecordingVisitor collects head and tail events as "head:name:depth" or "tail:name:depth".
     */
    static class RecordingVisitor implements NodeVisitor {
        List<String> events = new ArrayList<>();

        @Override
        public void head(Node node, int depth) {
            events.add("head:" + node.nodeName() + ":" + depth);
        }

        @Override
        public void tail(Node node, int depth) {
            events.add("tail:" + node.nodeName() + ":" + depth);
        }
    }

    @Test
    @DisplayName("TC12: two siblings: child with sibling exercises nextSibling non-null at depth>0 (B17→B18→B20)")
    public void test_TC12() {
        // GIVEN root with two children (c1, c2) to trigger non-null nextSibling at depth>0
        Element root = new Element("div");
        Element c1 = new Element("a");
        Element c2 = new Element("b");
        root.appendChild(c1);
        root.appendChild(c2);
        RecordingVisitor vis = new RecordingVisitor();

        // WHEN traverse is called
        NodeTraversor.traverse(vis, root);

        // THEN verify depth-first head/tail sequence
        // head:div at depth 0
        assertEquals("head:div:0", vis.events.get(0), "First event must be head of root at depth 0");
        // heads of both children at depth 1 should be present
        assertTrue(vis.events.contains("head:a:1"), "Should have visited head of first child at depth 1");
        assertTrue(vis.events.contains("head:b:1"), "Should have visited head of second child at depth 1");
        // final event must be tail of root at depth 0
        assertEquals("tail:div:0", vis.events.get(vis.events.size() - 1), "Last event must be tail of root at depth 0");
    }

    @Test
    @DisplayName("TC13: remove-first-child when sibling exists triggers replace/removed branch B9→B10 (nextSibling non-null removal)")
    public void test_TC13() {
        // GIVEN root with two children, and visitor that removes first child in head at depth=1
        Element root = new Element("div");
        Element c1 = new Element("x");
        Element c2 = new Element("y");
        root.appendChild(c1);
        root.appendChild(c2);
        // visitor that removes node at depth 1 and records visited names
        List<String> visited = new ArrayList<>();
        NodeVisitor v = new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                if (depth == 1) {
                    node.remove(); // removal to trigger B9->B10
                }
                visited.add("head:" + node.nodeName() + ":" + depth);
            }
            @Override
            public void tail(Node node, int depth) {
                visited.add("tail:" + node.nodeName() + ":" + depth);
            }
        };

        // WHEN traverse is called
        NodeTraversor.traverse(v, root);

        // THEN first child c1 should be removed, only c2 remains as child
        assertEquals(1, root.childNodeSize(), "After removal, root should have only one child");
        assertEquals("y", root.childNode(0).nodeName(), "Remaining child should be the second element 'y'");
        // c2 head must have been visited at depth 1
        assertTrue(visited.contains("head:y:1"), "Visitor must have visited head of 'y' at depth 1");
    }

    @Test
    @DisplayName("TC14: elements list with two elements loops twice in traverse(visitor, Elements) (B1→B2 twice then B3)")
    public void test_TC14() {
        // GIVEN two distinct Element roots in an Elements list
        Element e1 = new Element("p");
        Element e2 = new Element("span");
        Elements elems = new Elements(e1, e2);
        // RecordingVisitor to capture events
        RecordingVisitor vis = new RecordingVisitor();

        // WHEN traverse over Elements is called
        NodeTraversor.traverse(vis, elems);

        // THEN there should be exactly 2 head and 2 tail events (one for each element's root visit)
        long headCount = vis.events.stream().filter(ev -> ev.startsWith("head")).count();
        long tailCount = vis.events.stream().filter(ev -> ev.startsWith("tail")).count();
        assertEquals(2, headCount, "Should have two head events for two root elements");
        assertEquals(2, tailCount, "Should have two tail events for two root elements");
    }
}