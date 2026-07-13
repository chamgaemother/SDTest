package org.jsoup.select;

import org.jsoup.helper.Validate;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.Elements;
import org.jsoup.select.NodeFilter;
import org.jsoup.select.NodeVisitor;
import org.jsoup.select.NodeFilter.FilterResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NodeTraversor_traverse_0_Test {

    // Helper visitor that records head and tail calls
    static class RecordingVisitor implements NodeVisitor {
        final List<String> events = new ArrayList<>();
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
    @DisplayName("TC01_O1 visitor is null triggers Validate.notNull(visitor) exception at entry")
    void test_TC01_O1() {
        // visitor null triggers IllegalArgumentException on entry B1 false
        Node root = new Element("div");
        assertThrows(IllegalArgumentException.class, () -> NodeTraversor.traverse((NodeVisitor) null, root));
    }

    @Test
    @DisplayName("TC02_O1 root is null triggers Validate.notNull(root) exception at entry")
    void test_TC02_O1() {
        // root null triggers IllegalArgumentException after visitor check B2 true
        NodeVisitor visitor = new NodeVisitor() { public void head(Node node, int depth) {} public void tail(Node node, int depth) {} };
        assertThrows(IllegalArgumentException.class, () -> NodeTraversor.traverse(visitor, (Node) null));
    }

    @Test
    @DisplayName("TC03_O1 single-node tree yields one head and one tail call with no children")
    void test_TC03_O1() {
        // single node has no children → descend skipped, one head/tail at depth 0 (path covers B11→B12 and B20→B22)
        Element root = new Element("p");
        RecordingVisitor vis = new RecordingVisitor();
        NodeTraversor.traverse(vis, root);
        assertAll("single node head/tail",
            () -> assertEquals(1, vis.events.stream().filter(e->e.startsWith("head")).count()),
            () -> assertEquals(1, vis.events.stream().filter(e->e.startsWith("tail")).count()),
            () -> assertEquals("head:p:0", vis.events.get(0)),
            () -> assertEquals("tail:p:0", vis.events.get(1))
        );
    }

    @Test
    @DisplayName("TC04_O1 two-level tree descends into child then ascends and visits sibling absence")
    void test_TC04_O1() {
        // root→child only; should record head(root,0), head(child,1), tail(child,1), tail(root,0)
        Element root = new Element("div");
        Element child = new Element("span");
        root.appendChild(child);
        RecordingVisitor vis = new RecordingVisitor();
        NodeTraversor.traverse(vis, root);
        List<String> expected = List.of(
            "head:div:0",
            "head:span:1",
            "tail:span:1",
            "tail:div:0"
        );
        assertEquals(expected, vis.events);
    }

    @Test
    @DisplayName("TC05_O1 deep nested three-level tree covers multiple descents")
    void test_TC05_O1() {
        // build 3-level: root→c1→c2→c3; expect depths 0,1,2,3 heads then tails reverse
        Element root = new Element("div");
        Element c1 = new Element("a");
        Element c2 = new Element("b");
        Element c3 = new Element("c");
        root.appendChild(c1);
        c1.appendChild(c2);
        c2.appendChild(c3);
        RecordingVisitor vis = new RecordingVisitor();
        NodeTraversor.traverse(vis, root);
        List<Integer> depths = new ArrayList<>();
        vis.events.forEach(e -> depths.add(Integer.parseInt(e.split(":")[2])));
        // depths should be [0,1,2,3,3,2,1,0]
        assertEquals(List.of(0,1,2,3,3,2,1,0), depths);
    }

    @Test
    @DisplayName("TC06_O1 visitor.head removes child at depth>0 triggers remove branch and skip tail for removed")
    void test_TC06_O1() {
        // when depth==1 in head, remove the node -> branch B7→B9 remove path, skip tail for child
        Element root = new Element("div");
        Element child = new Element("p");
        root.appendChild(child);
        class Remover implements NodeVisitor {
            int headCount, tailCount;
            @Override
            public void head(Node node, int depth) {
                headCount++;
                if (depth == 1) node.remove();
            }
            @Override
            public void tail(Node node, int depth) {
                tailCount++;
            }
        }
        Remover v = new Remover();
        NodeTraversor.traverse(v, root);
        assertAll(
            () -> assertEquals(2, v.headCount, "two head calls (root and child)"),
            () -> assertEquals(1, v.tailCount, "only root tail")
        );
    }

    @Test
    @DisplayName("TC07_O1 visitor.head replaces child triggers replace branch path")
    void test_TC07_O1() {
        // when depth==1, replace child with new span => branch B8, then traverse new node
        Element root = new Element("div");
        Element child = new Element("p");
        root.appendChild(child);
        class Replacer implements NodeVisitor {
            final List<Node> tailed = new ArrayList<>();
            @Override
            public void head(Node node, int depth) {
                if (depth == 1) {
                    node.replaceWith(new Element("span"));
                }
            }
            @Override
            public void tail(Node node, int depth) {
                tailed.add(node);
            }
        }
        Replacer v = new Replacer();
        NodeTraversor.traverse(v, root);
        // expect one of the tailed nodes to be span
        boolean hasSpan = v.tailed.stream().anyMatch(n -> "span".equals(n.nodeName()));
        assertTrue(hasSpan, "should have tail call on the replacement span node");
    }

    @Test
    @DisplayName("TC08_O1 assertion enabled and node null triggers AssertionError at B15")
    void test_TC08_O1() {
        // stub a Node whose childNodeSize()>0 but childNode(0) returns null to hit assert node!=null
        Node broken = new Element("x") {
            @Override public int childNodeSize() { return 1; }
            @Override public Node childNode(int index) { return null; }
        };
        NodeVisitor v = (n,d)->{};
        // require assertions enabled (-ea)
        assertThrows(AssertionError.class, () -> NodeTraversor.traverse(v, broken));
    }

    @Test
    @DisplayName("TC09_O2 elements list is empty results in no traversal")
    void test_TC09_O2() {
        // empty Elements => B1 false, no head/tail
        RecordingVisitor v = new RecordingVisitor();
        Elements empty = new Elements();
        NodeTraversor.traverse(v, empty);
        assertTrue(v.events.isEmpty(), "no head or tail should be called");
    }

    @Test
    @DisplayName("TC10_O2 elements list with one element invokes single-element traversal")
    void test_TC10_O2() {
        // single element => traverse that element once
        Element el = new Element("div");
        RecordingVisitor v = new RecordingVisitor();
        Elements elems = new Elements(el);
        NodeTraversor.traverse(v, elems);
        assertFalse(v.events.isEmpty(), "should have at least one head/tail for the element");
    }

    @Test
    @DisplayName("TC11_O2 elements list traversal stops early when nested traverse throws STOP in filter overload")
    void test_TC11_O2() {
        // filter(elements): underlying filter.head returns STOP => should break the loop, no exception
        class StoppingFilter implements NodeFilter {
            int headCalls = 0;
            @Override public FilterResult head(Node node, int depth) {
                headCalls++;
                return FilterResult.STOP;
            }
            @Override public FilterResult tail(Node node, int depth) {
                return FilterResult.CONTINUE;
            }
        }
        StoppingFilter f = new StoppingFilter();
        Elements elems = new Elements(new Element("p"));
        // should not throw, and head called once
        assertDoesNotThrow(() -> NodeTraversor.filter(f, elems));
        assertEquals(1, f.headCalls, "filter.head should be called exactly once then stop");
    }
}