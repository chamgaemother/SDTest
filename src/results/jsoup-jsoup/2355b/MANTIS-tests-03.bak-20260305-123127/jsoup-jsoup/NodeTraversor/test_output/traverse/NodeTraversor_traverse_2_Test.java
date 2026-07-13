package org.jsoup.select;

import org.jsoup.nodes.Element;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NodeTraversor_traverse_2_Test {

    @Test
    @DisplayName("traverse over multiple Elements iterates each Element in sequence")
    public void test_TC17() {
        // GIVEN: a visitor that records head and tail events
        List<String> events = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(org.jsoup.nodes.Node node, int depth) {
                events.add(node.nodeName() + ":" + depth);
            }

            @Override
            public void tail(org.jsoup.nodes.Node node, int depth) {
                events.add(node.nodeName() + ":" + depth);
            }
        };
        // Elements with two distinct roots "a" and "b" -> ensures loop executes twice (B2 loop x2)
        Elements elements = new Elements(new Element("a"), new Element("b"));

        // WHEN: traverse over elements
        NodeTraversor.traverse(visitor, elements);

        // THEN: head and tail called once per root in sequence
        List<String> expected = List.of("a:0", "a:0", "b:0", "b:0");
        assertEquals(expected, events, "Expected head and tail called once per element in order");
    }

    @Test
    @DisplayName("traverse(elements) throws IllegalArgumentException when visitor is null")
    public void test_TC18() {
        // GIVEN: a null visitor and a non-null Elements
        NodeVisitor visitor = null;
        Elements elements = new Elements(new Element("div")); // ensures entry B0

        // WHEN/THEN: IllegalArgumentException is thrown due to Validate.notNull(visitor) at start
        assertThrows(IllegalArgumentException.class,
            () -> NodeTraversor.traverse(visitor, elements),
            "Expected IllegalArgumentException when visitor is null");
    }

    @Test
    @DisplayName("traverse propagates RuntimeException thrown in tail")
    public void test_TC19() {
        // GIVEN: a root with one child to reach depth=1 and trigger leaf tail exception (B12->B23->...->tail)
        Element root = new Element("div");
        Element child = new Element("span");
        root.appendChild(child);
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(org.jsoup.nodes.Node node, int depth) {
                // no-op
            }

            @Override
            public void tail(org.jsoup.nodes.Node node, int depth) {
                // throw at depth 1 to propagate immediately
                if (depth == 1) {
                    throw new RuntimeException("tail-fail");
                }
            }
        };

        // WHEN/THEN: RuntimeException with message "tail-fail" propagates
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> NodeTraversor.traverse(visitor, root),
            "Expected RuntimeException from tail at depth 1");
        assertEquals("tail-fail", ex.getMessage());
    }
}