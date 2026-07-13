package org.jsoup.select;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.Element;
import org.jsoup.select.NodeFilter;
import org.jsoup.select.NodeFilter.FilterResult;
import org.jsoup.select.NodeTraversor;

import static org.junit.jupiter.api.Assertions.*;
public class NodeTraversor_traverse_2_Test {

    @Test
    @DisplayName("filter.head returns STOP at root causes immediate return without tail")
    public void test_TC14() {
        // GIVEN: a root element and a filter whose head always returns STOP
        Element root = new Element("div");
        NodeFilter filter = new NodeFilter() {
            @Override
            public FilterResult head(org.jsoup.nodes.Node node, int depth) {
                // depth==0 on root, return STOP to trigger immediate halt
                return FilterResult.STOP;
            }
            @Override
            public FilterResult tail(org.jsoup.nodes.Node node, int depth) {
                // should never be called
                return FilterResult.CONTINUE;
            }
        };
        // WHEN: filtering the root node
        FilterResult res = NodeTraversor.filter(filter, (org.jsoup.nodes.Node) root);
        // THEN: should stop immediately at head, without tail
        assertEquals(FilterResult.STOP, res);
    }

    @Test
    @DisplayName("filter.head returns SKIP_CHILDREN skips descent but still tails and returns CONTINUE at root")
    public void test_TC15() {
        // GIVEN: a root with one child, but head returns SKIP_CHILDREN to avoid descent
        Element root = new Element("div");
        Element child = new Element("span");
        root.appendChild(child);
        NodeFilter filter = new NodeFilter() {
            @Override
            public FilterResult head(org.jsoup.nodes.Node node, int depth) {
                // At root (depth 0), skip children so descent is bypassed
                return FilterResult.SKIP_CHILDREN;
            }
            @Override
            public FilterResult tail(org.jsoup.nodes.Node node, int depth) {
                // Always continue after tail
                return FilterResult.CONTINUE;
            }
        };
        // WHEN: filtering the root node
        FilterResult res = NodeTraversor.filter(filter, (org.jsoup.nodes.Node) root);
        // THEN: children are skipped, but tail on root is still called, and final result is CONTINUE
        assertEquals(FilterResult.CONTINUE, res);
    }

    @Test
    @DisplayName("filter.tail returns STOP during ascent interrupts traversal")
    public void test_TC16() {
        // GIVEN: a root with one child; head always CONTINUE so we descend to leaf,
        // tail returns STOP at leaf (depth 1) causing traversal to abort
        Element root = new Element("div");
        Element child = new Element("p");
        root.appendChild(child);
        NodeFilter filter = new NodeFilter() {
            @Override
            public FilterResult head(org.jsoup.nodes.Node node, int depth) {
                // Always descend into children
                return FilterResult.CONTINUE;
            }
            @Override
            public FilterResult tail(org.jsoup.nodes.Node node, int depth) {
                // At leaf (depth == 1) return STOP to interrupt on tail
                return (depth == 1) ? FilterResult.STOP : FilterResult.CONTINUE;
            }
        };
        // WHEN: filtering the root node
        FilterResult res = NodeTraversor.filter(filter, (org.jsoup.nodes.Node) root);
        // THEN: should stop when tail at leaf returns STOP
        assertEquals(FilterResult.STOP, res);
    }

    @Test
    @DisplayName("filter.REMOVE removes leaf and continues traversal to siblings then returns CONTINUE")
    public void test_TC17() {
        // GIVEN: a list root with two items; head always CONTINUE; tail returns REMOVE for first leaf
        Element root = new Element("ul");
        Element item1 = new Element("li");
        Element item2 = new Element("li");
        root.appendChild(item1);
        root.appendChild(item2);
        NodeFilter filter = new NodeFilter() {
            @Override
            public FilterResult head(org.jsoup.nodes.Node node, int depth) {
                // Always allow descent
                return FilterResult.CONTINUE;
            }
            @Override
            public FilterResult tail(org.jsoup.nodes.Node node, int depth) {
                // Remove first item at its tail position (depth 1), else continue
                if (node == item1) {
                    return FilterResult.REMOVE;
                }
                return FilterResult.CONTINUE;
            }
        };
        // WHEN: filtering the root node
        FilterResult res = NodeTraversor.filter(filter, (org.jsoup.nodes.Node) root);
        // THEN: first child should be pruned, traversal continues to second, final result is CONTINUE
        assertFalse(root.childNodes().contains(item1), "item1 should have been removed");
        assertEquals(FilterResult.CONTINUE, res);
    }
}