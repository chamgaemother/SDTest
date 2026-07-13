package org.jsoup.select;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.jsoup.select.NodeVisitor; // Added import statement for NodeVisitor
import org.jsoup.select.NodeTraversor; // Added import statement for NodeTraversor

public class NodeTraversor_traverse_1_Test {

    @Test
    @DisplayName("visitor.head throws RuntimeException to propagate exception immediately")
    public void test_TC14() {
        // GIVEN an Element root with no children and a visitor whose head always throws
        Element root = new Element("div");
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node n, int d) {
                // force exception at first head call (depth=0, root node)
                throw new RuntimeException("fail");
            }
            @Override
            public void tail(Node n, int d) {
            }
        };
        // WHEN & THEN: expect the traverse call to immediately propagate the RuntimeException
        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> {
            NodeTraversor.traverse(visitor, (Node) root);
        });
        Assertions.assertEquals("fail", thrown.getMessage());
    }

    @Test
    @DisplayName("Two-level tree with sibling at depth>0 triggers correct head/tail order covering deep sibling edges")
    public void test_TC15() {
        // GIVEN a root with two children, one of which has a nested child to exercise depth>0 and sibling branches
        Element root = new Element("root");
        Element c1 = new Element("c1");
        Element c1_1 = new Element("c1_1");
        Element c2 = new Element("c2");
        root.appendChild(c1);      // depth 1 child
        c1.appendChild(c1_1);      // depth 2 nested child
        root.appendChild(c2);      // sibling at depth 1
        List<String> events = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node n, int d) {
                // record head events with node name and depth
                events.add("H:" + n.nodeName() + "/" + d);
            }
            @Override
            public void tail(Node n, int d) {
                // record tail events
                events.add("T:" + n.nodeName() + "/" + d);
            }
        };
        // WHEN
        NodeTraversor.traverse(visitor, (Node) root);
        // THEN: expect head/tail calls in the specific sequence covering descend, ascend, sibling, and root completion
        List<String> expected = List.of(
            "H:root/0",
            "H:c1/1",
            "H:c1_1/2",
            "T:c1_1/2",
            "T:c1/1",
            "H:c2/1",
            "T:c2/1",
            "T:root/0"
        );
        Assertions.assertEquals(expected, events);
    }

    @Test
    @DisplayName("visitor.head removes intermediate node when nextSibling is null to exercise removed+ascend branch")
    public void test_TC16() {
        // GIVEN a root with a single child that will be removed at depth 1 to force the removed+ascend path
        Element root = new Element("div");
        Element child = new Element("span");
        root.appendChild(child);
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node n, int d) {
                // remove the only child when at depth 1
                if (d == 1) {
                    n.remove();
                }
            }
            @Override
            public void tail(Node n, int d) {
                // no-op tail
            }
        };
        // WHEN
        NodeTraversor.traverse(visitor, (Node) root);
        // THEN: the child should have been removed, leaving the root with zero children
        Assertions.assertEquals(0, root.childNodeSize());
    }
}