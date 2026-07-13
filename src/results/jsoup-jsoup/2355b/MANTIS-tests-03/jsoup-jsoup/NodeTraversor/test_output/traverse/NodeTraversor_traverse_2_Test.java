package org.jsoup.select;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.jsoup.nodes.Elements; // Correct import for Elements
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NodeTraversor_traverse_2_Test {

    @Test
    @DisplayName("three-level chain with no siblings exercises ascend-loop skip-inner-tail branch (B16→B17→B18→B20→B21)")
    public void test_TC16() {
        // GIVEN a three-level chain: div -> b -> i (no siblings) to force descent then full ascend
        Element leaf = new Element("i");
        Element mid = new Element("b").appendChild(leaf);
        Element root = new Element("div").appendChild(mid);
        List<String> head = new ArrayList<>();
        List<String> tail = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                head.add(node.nodeName() + ":" + depth);
            }
            @Override
            public void tail(Node node, int depth) {
                tail.add(node.nodeName() + ":" + depth);
            }
        };

        // WHEN traverse is called, should visit head and tail in proper DFS order
        NodeTraversor.traverse(visitor, new Elements(root));

        // THEN head visits in order div:0, b:1, i:2
        assertEquals(List.of("div:0", "b:1", "i:2"), head,
            "Head list should reflect pre-order traversal with correct depths");
        // AND tail visits in reverse order i:2, b:1, div:0
        assertEquals(List.of("i:2", "b:1", "div:0"), tail,
            "Tail list should reflect post-order traversal with correct depths");
    }

    @Test
    @DisplayName("two siblings under parent exercises removal-then-ascend branch (B6→B7→B9→B10)")
    public void test_TC17() {
        // GIVEN a root with two children a and b; visitor.head removes first child to trigger removal branch
        Element c1 = new Element("a");
        Element c2 = new Element("b");
        Element root = new Element("div");
        root.appendChild(c1);
        root.appendChild(c2);
        List<String> tail = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                if (node == c1) {
                    // remove the first child during head to force removal logic
                    c1.remove();
                }
            }
            @Override
            public void tail(Node node, int depth) {
                tail.add(node.nodeName() + ":" + depth);
            }
        };

        // WHEN traversing, the removed node should not produce a tail call and root should end with only one child
        NodeTraversor.traverse(visitor, new Elements(root));

        // THEN only the second child remains
        assertEquals(1, root.childNodeSize(), "After removal, only one child should remain on root");
        // AND tail contains b:1, but not a:1
        assertTrue(tail.contains("b:1"), "Tail should include the surviving sibling at depth 1");
        assertFalse(tail.contains("a:1"), "Tail should not include the removed node");
    }
}