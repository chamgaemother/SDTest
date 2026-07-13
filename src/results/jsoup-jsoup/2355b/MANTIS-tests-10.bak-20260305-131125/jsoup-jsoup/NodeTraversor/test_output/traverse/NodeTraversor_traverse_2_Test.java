package org.jsoup.select;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class NodeTraversor_traverse_2_Test {
    /**
     * TrackVisitor records head and tail calls with node name and depth.
     */
    static class TrackVisitor implements NodeVisitor {
        List<String> sequence = new ArrayList<>();
        int tailCount = 0;

        @Override
        public void head(Node node, int depth) {
            sequence.add("head:" + node.nodeName() + "@" + depth);
        }

        @Override
        public void tail(Node node, int depth) {
            sequence.add("tail:" + node.nodeName() + "@" + depth);
            tailCount++;
        }
    }

    @Test
    @DisplayName("TC09: Removal branch when current node is the last child triggers ascend via origSize!=childSize and nextSibling==null")
    void test_TC09() {
        // GIVEN: root with single child c; visitor.head removes c when depth==1 -> triggers removal branch
        Element root = new Element("div");
        Element c = new Element("p");
        root.appendChild(c);
        TrackVisitor visitor = new TrackVisitor() {
            @Override
            public void head(Node node, int depth) {
                super.head(node, depth);
                // remove the node when we reach child at depth 1
                if (node == c && depth == 1) {
                    node.remove();
                }
            }
        };
        // WHEN
        NodeTraversor.traverse(visitor, root);
        // THEN: root has no children, and removed node never saw a tail call
        assertEquals(0, root.childNodeSize(), "Child should be removed during traversal");
        boolean sawTailRemoved = visitor.sequence.stream().anyMatch(s -> s.startsWith("tail:p@"));
        assertFalse(sawTailRemoved, "Removed node should not have tail callback");
    }

    @Test
    @DisplayName("TC10: Deep three-level tree triggers multiple ascends in inner loop (nextSibling==null&&depth>0)")
    void test_TC10() {
        // GIVEN: linear chain root->c1->c2, no siblings -> forces descend then ascend twice
        Element root = new Element("a");
        Element c1 = new Element("b");
        Element c2 = new Element("c");
        root.appendChild(c1);
        c1.appendChild(c2);
        TrackVisitor visitor = new TrackVisitor();
        // WHEN
        NodeTraversor.traverse(visitor, root);
        // THEN: head and tail for c at depth 2, and total tailCount should be 3 (c, b, a)
        assertTrue(visitor.sequence.contains("head:c@2"), "Should record head for grandchild at depth 2");
        int headIndex = visitor.sequence.indexOf("head:c@2");
        int tailIndex = visitor.sequence.indexOf("tail:c@2");
        assertTrue(tailIndex > headIndex, "Tail of c should occur after its head");
        assertEquals(3, visitor.tailCount, "Should have three tail callbacks for c, b, and a");
    }

    @Test
    @DisplayName("TC11: Sibling branch when node has nextSibling and depth>0 exercises sibling traversal at depth 1")
    void test_TC11() {
        // GIVEN: root->c1 and c2 siblings at depth 1 -> ensures B16->B17->B18->B20->B22 path
        Element root = new Element("div");
        Element c1 = new Element("x");
        Element c2 = new Element("y");
        root.appendChild(c1);
        root.appendChild(c2);
        TrackVisitor visitor = new TrackVisitor();
        // WHEN
        NodeTraversor.traverse(visitor, root);
        // THEN: head:y@1 occurs after tail:x@1, and at least 4 tail events (x, x ascends or y, and root)
        int tailX = visitor.sequence.indexOf("tail:x@1");
        int headY = visitor.sequence.indexOf("head:y@1");
        assertTrue(headY > tailX, "Should visit head of second sibling after tail of first sibling");
        assertTrue(visitor.tailCount >= 4, "Should have at least four tail callbacks for siblings and root");
    }
}