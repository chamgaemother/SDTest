package org.jsoup.select;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Parser.HtmlSettings;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class NodeTraversor_traverse_1_Test {

    @Test
    @DisplayName("TC12: remove first of two siblings in head: triggers non-last-sibling remove and continue traversal")
    public void test_TC12() {
        // GIVEN a root with two children a and b
        Element root = new Element("root");
        TextNode a = new TextNode("a");
        TextNode b = new TextNode("b");
        root.appendChild(a);
        root.appendChild(b);
        List<String> events = new ArrayList<>();
        // Visitor removes a in head of first child, covering B9->B10 removal branch
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node n, int d) {
                events.add(n.outerHtml());
                if (n == a) {
                    n.remove(); // remove non-last sibling, so origSize>childSize triggers removal path
                }
            }
            @Override
            public void tail(Node n, int d) {
                // No action needed for tail in this test
            }
        };

        // WHEN traverse starting at first child
        NodeTraversor.traverse(visitor, root.childNode(0));

        // THEN the second child head must have been called, and only b remains
        assertTrue(events.contains("b"), "Expected head called for 'b' after removal of first sibling");
        assertEquals(1, root.childNodeSize(), "Expected root to have only one child after removal");
    }

    @Test
    @DisplayName("TC13: deep tree with nested single child triggers ancestor-tail loop branch and descent")
    public void test_TC13() {
        // GIVEN a nested single-child deep tree <a><b><c/></b></a>
        Node root = Parser.parseFragment("<a><b><c/></b></a>", new HtmlSettings()).get(0);
        List<String> records = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node n, int d) {
                records.add("head" + d); // record head at each depth
            }
            @Override
            public void tail(Node n, int d) {
                records.add("tail" + d); // record tail at each depth
            }
        };

        // WHEN traverse from root
        NodeTraversor.traverse(visitor, root);

        // THEN head0, head1, head2 then tail2, tail1, tail0 sequence should occur
        List<String> expected = List.of("head0", "head1", "head2", "tail2", "tail1", "tail0");
        assertEquals(expected, records, "Expected depth-first order with correct head/tail calls and ascend-loop");
    }
}