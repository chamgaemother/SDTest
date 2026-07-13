package org.jsoup.select;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class NodeTraversor_traverse_1_Test {

    @Test
    @DisplayName("three-level tree triggers multi-level ascend loop (depth > 0) hitting B17→B18 path")
    public void test_TC12() {
        // Set up a three-level tree: root -> c1 -> c2
        Element root = new Element("root");
        Element c1 = new Element("c1");
        Element c2 = new Element("c2");
        root.appendChild(c1);
        c1.appendChild(c2);

        // Collect head/tail calls
        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                // depth increases on descend, so at c2 depth==2 triggers multi-level ascend
                calls.add("head:" + node.nodeName() + ":" + depth);
            }

            @Override
            public void tail(Node node, int depth) {
                calls.add("tail:" + node.nodeName() + ":" + depth);
            }
        };

        // Execute traversal
        NodeTraversor.traverse(visitor, root);

        // Expect head then tail in depth-first order:
        List<String> expected = Arrays.asList(
            "head:root:0",
            "head:c1:1",
            "head:c2:2",
            "tail:c2:2",  // ascend from deepest node
            "tail:c1:1",  // continue ascend
            "tail:root:0" // finish at root
        );
        assertEquals(expected, calls);
    }

    @Test
    @DisplayName("remove at depth>1 with nextSibling non-null skips tail for removed and ascends correctly")
    public void test_TC13() {
        // Build tree root->c1->c2 and a sibling of c1
        Element root = new Element("div");
        Element c1 = new Element("p");
        Element c2 = new Element("span");
        Element sibling = new Element("a");
        root.appendChild(c1);
        c1.appendChild(c2);
        root.appendChild(sibling);
        // c2 has a next sibling non-null? c2.nextSibling()==null, but removal yields nextSibling of c1 -> sibling

        List<String> calls = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                if (node == c2) {
                    node.remove(); // remove at depth>1
                    calls.add("head:removed:" + node.nodeName() + ":" + depth);
                } else {
                    calls.add("head:" + node.nodeName() + ":" + depth);
                }
            }

            @Override
            public void tail(Node node, int depth) {
                calls.add("tail:" + node.nodeName() + ":" + depth);
            }
        };

        // Execute traversal
        NodeTraversor.traverse(visitor, root);

        // After removal of c2, its tail is skipped. The traversal should continue with sibling at depth 1.
        List<String> expected = Arrays.asList(
            "head:div:0",
            "head:p:1",
            "head:removed:span:2", // removed so no tail for span
            "head:a:1",            // next sibling of c1 once c2 removed
            "tail:a:1",
            "tail:p:1",
            "tail:div:0"
        );
        assertEquals(expected, calls);
    }
}