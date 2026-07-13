package org.jsoup.select;

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

public class NodeTraversor_traverse_2_Test {

    @Test
    @DisplayName("TC11: traverse(NodeVisitor, Elements) with two elements exercises loop-N >1 in Elements overload")
    void test_TC11() {
        // GIVEN two sibling elements with no children to exercise the for-loop over elements (path B0→B1→B2(loop×2)→B1→B3)
        Element p = new Element("p");
        Element span = new Element("span");
        Elements els = new Elements(p, span);
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                // depth stays 0 since no children -> cover visitor.head then immediate tail
                calls.add("H:" + node.nodeName() + depth);
            }
            @Override
            public void tail(Node node, int depth) {
                calls.add("T:" + node.nodeName() + depth);
            }
        };
        // WHEN
        NodeTraversor.traverse(visitor, els);
        // THEN exactly two head/tail pairs in iteration order
        List<String> expected = List.of("H:p0", "T:p0", "H:span0", "T:span0");
        assertEquals(expected, calls, "Should record head/tail calls for each element in order");
    }

    @Test
    @DisplayName("TC12: exception in visitor.head aborts traversal immediately via thrown RuntimeException")
    void test_TC12() {
        // GIVEN a visitor whose head always throws to exercise early exit (path B0→B1→B2→B5→B24)
        Element p = new Element("div");
        Elements els = new Elements(p);
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                throw new RuntimeException("abort");
            }
            @Override
            public void tail(Node node, int depth) {
                fail("tail should not be called after head throws");
            }
        };
        // WHEN / THEN: expect the RuntimeException("abort") to propagate immediately
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> NodeTraversor.traverse(visitor, els),
            "Traversal should abort when head throws");
        assertEquals("abort", ex.getMessage());
    }

    @Test
    @DisplayName("TC13: deep tree of depth 2 exercises two-level descent and ascent loops")
    void test_TC13() {
        // GIVEN a root->child1->child2 to exercise descent into childNodeSize()>0 and ascend loops (B0→B1→B11→B12…→B21→B24)
        Element root = new Element("root");
        Element child1 = new Element("c1");
        Element child2 = new Element("c2");
        root.appendChild(child1);    // depth 1
        child1.appendChild(child2);  // depth 2
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                calls.add("h:" + node.nodeName() + depth);
            }
            @Override
            public void tail(Node node, int depth) {
                calls.add("t:" + node.nodeName() + depth);
            }
        };
        // WHEN
        NodeTraversor.traverse(visitor, root);
        // THEN head/tail at depths 0,1,2 in correct DF order
        List<String> expected = List.of(
            "h:root0", "h:c10", "h:c21",
            "t:c21", "t:c10", "t:root0"
        );
        assertEquals(expected, calls, "Should traverse down and then up the deep tree in correct order");
    }
}