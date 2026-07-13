package org.jsoup.select;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NodeTraversor_traverse_1_Test {

    @Test
    @DisplayName("three-level nested tree exercises multiple descend (B12) and ascend-without-siblings (B16→B19) branches")
    public void test_TC10() {
        // GIVEN a three-level nested tree: root -> l1 -> l2
        Element root = new Element("div");
        Element l1 = new Element("span");
        Element l2 = new Element("em");
        root.appendChild(l1);
        l1.appendChild(l2);

        // Capture the sequence of head/tail calls
        List<String> seq = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                seq.add(node.nodeName() + "@" + depth);
            }

            @Override
            public void tail(Node node, int depth) {
                seq.add(node.nodeName() + "@" + depth);
            }
        };

        // WHEN: traverse should descend twice (depths 1 and 2) then ascend back
        NodeTraversor.traverse(visitor, root); // Changed to use Node instead of Elements

        // THEN: visitor should record head then tail at each depth in depth-first order
        List<String> expected = List.of(
                "div@0", // head root at depth 0
                "span@1", // head level1 at depth 1
                "em@2",   // head level2 at depth 2
                "em@2",   // tail level2 at depth 2 (no siblings → ascend)
                "span@1", // tail level1 at depth 1 (after finishing children)
                "div@0"   // tail root at depth 0
        );
        assertEquals(expected, seq);
    }

    @Test
    @DisplayName("root with two children exercises sibling branch (B16→B17→B20→B22) at depth>0")
    public void test_TC11() {
        // GIVEN a root with two immediate children to force sibling handling at depth>0
        Element root = new Element("div");
        Element a = new Element("a");
        Element b = new Element("b");
        root.appendChild(a);
        root.appendChild(b);

        // Capture sequence of visitor calls (only nodeName, depth ignored)
        List<String> seq = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                seq.add(node.nodeName());
            }

            @Override
            public void tail(Node node, int depth) {
                seq.add(node.nodeName());
            }
        };

        // WHEN: traverse over root with two children
        NodeTraversor.traverse(visitor, root); // Changed to use Node instead of Elements

        // THEN: expect [div,a,a,b,b,div] – head and tail of each in order including root
        List<String> expected = List.of(
                "div", // head root
                "a",   // head child a
                "a",   // tail child a (no children; depth>0, sibling exists)
                "b",   // head child b (sibling branch)
                "b",   // tail child b
                "div"  // tail root
        );
        assertEquals(expected, seq);
    }

    @Test
    @DisplayName("Elements overload with multiple elements exercises loop branch (B1→B2×2→B3)")
    public void test_TC12() {
        // GIVEN two separate element roots in an Elements collection
        Element x = new Element("x");
        Element y = new Element("y");
        Elements els = new Elements(x, y);

        // Capture sequence of visitor calls for each root
        List<String> seq = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                seq.add(node.nodeName());
            }

            @Override
            public void tail(Node node, int depth) {
                seq.add(node.nodeName());
            }
        };

        // WHEN: traverse overload invoked with Elements
        NodeTraversor.traverse(visitor, els); // Changed to use Elements directly

        // THEN: each element should be visited head and tail in sequence, in order x then y
        List<String> expected = List.of(
                "x", // head x
                "x", // tail x
                "y", // head y
                "y"  // tail y
        );
        assertEquals(expected, seq);
    }
}