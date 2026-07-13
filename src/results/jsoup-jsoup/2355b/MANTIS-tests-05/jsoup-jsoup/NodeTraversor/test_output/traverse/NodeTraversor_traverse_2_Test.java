package org.jsoup.select;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.select.NodeTraversor; // Added import for NodeTraversor
import org.jsoup.select.NodeVisitor; // Corrected import for NodeVisitor

public class NodeTraversor_traverse_2_Test {

    @Test
    @DisplayName("two-child root without removals triggers early return via sibling-stop path (B16→B17→B18→B20→B21)")
    public void test_TC12() {
        // GIVEN: a root with exactly two children, no mutations in head/tail => depth-first should visit root and first child only
        Element root = new Element("ul");
        Element li1 = new Element("li1");
        Element li2 = new Element("li2");
        root.appendChild(li1);
        root.appendChild(li2);
        List<String> calls = new ArrayList<>();
        // only record head calls to observe early stop before second child
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                calls.add(node.nodeName() + ":" + depth);
            }
            @Override
            public void tail(Node node, int depth) {
                // no-op to avoid modifying structure
            }
        };

        // WHEN: traverse is invoked on the root
        NodeTraversor.traverse(visitor, root);

        // THEN: root at depth 0 and first child at depth 1 are visited, but second child is not
        assertTrue(calls.contains("ul:0"), "Expected head called on root at depth 0");
        assertTrue(calls.contains("li1:1"), "Expected head called on first child at depth 1");
        assertFalse(calls.contains("li2:1"), "Did not expect head call on second child due to early stop");
    }

    @Test
    @DisplayName("deep chain depth=2 without siblings exercises multi-level ascend loop (B12→B23→B1→...→B16→B20→B22→B23)")
    public void test_TC13() {
        // GIVEN: a nested structure div->span->i with no siblings at each level => forces descend twice then ascend twice
        Element root = new Element("div");
        Element child = new Element("span");
        Element grand = new Element("i");
        child.appendChild(grand);
        root.appendChild(child);
        List<String> calls = new ArrayList<>();
        // record both head and tail to verify full depth-first
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                calls.add("head:" + node.nodeName() + ":" + depth);
            }
            @Override
            public void tail(Node node, int depth) {
                calls.add("tail:" + node.nodeName() + ":" + depth);
            }
        };

        // WHEN: perform traversal
        NodeTraversor.traverse(visitor, root);

        // THEN: calls should reflect correct DFS order: head(div,0)->head(span,1)->head(i,2)->tail(i,2)->tail(span,1)->tail(div,0)
        List<String> expected = List.of(
            "head:div:0",
            "head:span:1",
            "head:i:2",
            "tail:i:2",
            "tail:span:1",
            "tail:div:0"
        );
        assertEquals(expected, calls, "Traversal should head and tail nodes in proper depth-first sequence");
    }
}