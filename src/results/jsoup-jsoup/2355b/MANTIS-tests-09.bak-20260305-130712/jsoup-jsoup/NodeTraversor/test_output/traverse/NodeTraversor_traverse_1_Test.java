package org.jsoup.select;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NodeTraversor_traverse_1_Test {

    @Test
    @DisplayName("TC12: traverse(elements) throws IllegalArgumentException when elements contains a null entry")
    public void test_TC12() {
        // GIVEN a visitor that does nothing but is non-null
        NodeVisitor visitor = new NodeVisitor() {
            @Override public void head(Node node, int depth) { }
            @Override public void tail(Node node, int depth) { }
        };
        // GIVEN an Elements list containing a null element to trigger nested-null check
        Elements els = new Elements((Element) null);
        // WHEN / THEN: should throw IllegalArgumentException due to Validate.notNull(root)
        Executable call = () -> NodeTraversor.traverse(visitor, els);
        assertThrows(IllegalArgumentException.class, call, 
            // Inline comment: the single null Element causes Validate.notNull(root) in nested traverse to fail
            "Expected IllegalArgumentException when elements contains a null entry"
        );
    }

    @Test
    @DisplayName("TC13: traverse(elements) with single-element list invokes visitor exactly once")
    public void test_TC13() {
        // GIVEN a single Element with tag name 'x', depth starts at 0, no children
        Element el = new Element("x");
        Elements els = new Elements(el);
        // GIVEN a list to capture head/tail calls
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                // head called once on root at depth 0
                calls.add("H" + node.nodeName() + depth);
            }
            @Override
            public void tail(Node node, int depth) {
                // tail called once on root at depth 0
                calls.add("T" + node.nodeName() + depth);
            }
        };
        // WHEN traversal is executed over the single-element list
        NodeTraversor.traverse(visitor, els);
        // THEN exactly two calls in order: head then tail at depth=0
        // Inline comment: with one element and no children, visitor.head then visitor.tail should be called on that element at depth 0
        assertEquals(2, calls.size(), "Expected exactly two callbacks");
        assertEquals("Hx0", calls.get(0), "First call should be head(x,0)");
        assertEquals("Tx0", calls.get(1), "Second call should be tail(x,0)");
    }
}