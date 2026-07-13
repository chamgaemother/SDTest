package org.jsoup.select;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeFilter.FilterResult;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NodeTraversor_traverse_2_Test {

    @Test
    @DisplayName("three-level nested tree exercises multiple descend (childNodeSize>0) and ascend-without-siblings (node.nextSibling==null && depth>0) branches")
    public void test_TC10() {
        // Setup a 3-level tree: div -> span -> em. This forces descent and then ascend without siblings.
        Element root = new Element("div");
        Element l1 = new Element("span");
        Element l2 = new Element("em");
        root.appendChild(l1);
        l1.appendChild(l2);
        List<String> seq = new ArrayList<>();
        org.jsoup.select.NodeVisitor visitor = new org.jsoup.select.NodeVisitor() {
            @Override public void head(Node n, int d) { seq.add(n.nodeName() + "@" + d); }
            @Override public void tail(Node n, int d) { seq.add(n.nodeName() + "@" + d); }
        };

        // Execute traverse: depth-first visit
        org.jsoup.select.NodeTraversor.traverse(visitor, root);

        // Expect head and tail in strict depth-first order: div@0, span@1, em@2, em@2, span@1, div@0
        assertEquals(
            List.of("div@0", "span@1", "em@2", "em@2", "span@1", "div@0"),
            seq
        );
    }

    @Test
    @DisplayName("root with two children exercises sibling branch (node.nextSibling!=null && depth>0) path")
    public void test_TC11() {
        // Setup a root with two direct children to force sibling iteration at same depth
        Element root = new Element("div");
        Element a = new Element("a");
        Element b = new Element("b");
        root.appendChild(a);
        root.appendChild(b);
        List<String> seq = new ArrayList<>();
        org.jsoup.select.NodeVisitor visitor = new org.jsoup.select.NodeVisitor() {
            @Override public void head(Node n, int d) { seq.add(n.nodeName()); }
            @Override public void tail(Node n, int d) { seq.add(n.nodeName()); }
        };

        // Traverse will call head/tail for div, then a, then a tail, then b, then b tail, then div tail
        org.jsoup.select.NodeTraversor.traverse(visitor, root);

        assertEquals(
            List.of("div", "a", "a", "b", "b", "div"),
            seq
        );
    }

    @Test
    @DisplayName("multiple Elements overload traverses each element subtree in order (loop>1)")
    public void test_TC12() {
        // Prepare two root elements x and y to test the Elements overload looping
        Element x = new Element("x");
        Element y = new Element("y");
        Elements els = new Elements(x, y);
        List<String> seq = new ArrayList<>();
        org.jsoup.select.NodeVisitor visitor = new org.jsoup.select.NodeVisitor() {
            @Override public void head(Node n, int d) { seq.add(n.nodeName()); }
            @Override public void tail(Node n, int d) { seq.add(n.nodeName()); }
        };

        // Should traverse x subtree then y subtree
        org.jsoup.select.NodeTraversor.traverse(visitor, els);

        assertEquals(
            List.of("x", "x", "y", "y"),
            seq
        );
    }

    @Test
    @DisplayName("iterate Elements overload stops on first STOP filter result in filter overload")
    public void test_TC13() {
        // Prepare two elements a and b; filter should stop when encountering a at head
        Element a = new Element("a");
        Element b = new Element("b");
        Elements els = new Elements(a, b);
        List<String> seen = new ArrayList<>();
        org.jsoup.select.NodeFilter filter = new org.jsoup.select.NodeFilter() {
            @Override public FilterResult head(Node n, int d) {
                seen.add(n.nodeName());
                // STOP on first element's head, skip everything else
                return n == a ? FilterResult.STOP : FilterResult.CONTINUE;
            }
            @Override public FilterResult tail(Node n, int d) {
                return FilterResult.CONTINUE;
            }
        };

        // Running filter on Elements should stop after the first STOP and not process b
        org.jsoup.select.NodeTraversor.filter(filter, els);

        assertEquals(
            List.of("a"),
            seen
        );
    }
}