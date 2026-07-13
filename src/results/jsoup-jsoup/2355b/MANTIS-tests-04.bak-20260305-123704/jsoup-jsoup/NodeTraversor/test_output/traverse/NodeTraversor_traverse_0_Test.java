package org.jsoup.select;

import static org.junit.jupiter.api.Assertions.*;

import org.jsoup.helper.Validate;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.Elements; // Correct import for Elements
import org.jsoup.parser.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

// Importing NodeVisitor interface
import org.jsoup.select.NodeVisitor;

public class NodeTraversor_traverse_0_Test {

    // Helper visitor that records head/tail calls and depths
    static class RecordingVisitor implements NodeVisitor {
        final List<String> sequence = new ArrayList<>();
        int headCount = 0;
        int tailCount = 0;
        int maxDepth = 0;

        @Override
        public void head(Node node, int depth) {
            headCount++;
            sequence.add("head" + depth);
            if (depth > maxDepth) maxDepth = depth;
        }

        @Override
        public void tail(Node node, int depth) {
            tailCount++;
            sequence.add("tail" + depth);
        }
    }

    // Helper visitor that removes node in head
    static class RemovingVisitor implements NodeVisitor {
        int headCount = 0;
        int tailCount = 0;
        @Override
        public void head(Node node, int depth) {
            headCount++;
            node.remove(); // simulate removal
        }
        @Override public void tail(Node node, int depth) { tailCount++; }
    }

    // Helper visitor that replaces node in head
    static class ReplacingVisitor implements NodeVisitor {
        final List<String> sequence = new ArrayList<>();
        @Override
        public void head(Node node, int depth) {
            Node replacement = new Element(Tag.valueOf("p"), "");
            node.replaceWith(replacement); // simulate replace
            sequence.add("head" + depth);
        }
        @Override
        public void tail(Node node, int depth) {
            sequence.add("tail" + depth);
        }
    }

    @Test
    @DisplayName("TC01_O1: Null NodeVisitor causes IllegalArgumentException at validation")
    public void test_TC01_O1() {
        // Passing null visitor triggers Validate.notNull(visitor)
        Node root = new Element(Tag.valueOf("div"), "");
        assertThrows(IllegalArgumentException.class, () -> NodeTraversor.traverse((NodeVisitor) null, root));
    }

    @Test
    @DisplayName("TC02_O1: Null root Node causes IllegalArgumentException at validation")
    public void test_TC02_O1() {
        // Passing null root triggers Validate.notNull(root)
        RecordingVisitor visitor = new RecordingVisitor();
        assertThrows(IllegalArgumentException.class, () -> NodeTraversor.traverse(visitor, (Node) null));
    }

    @Test
    @DisplayName("TC03_O1: Single node with no children invokes head and tail once")
    public void test_TC03_O1() {
        // Single root with no children -> one head and one tail at depth=0
        RecordingVisitor v = new RecordingVisitor();
        Element root = new Element(Tag.valueOf("div"), "");
        NodeTraversor.traverse(v, root);
        assertEquals(1, v.headCount);
        assertEquals(1, v.tailCount);
        assertEquals(0, v.maxDepth);
    }

    @Test
    @DisplayName("TC04_O1: Two-level tree descends into child and ascends back")
    public void test_TC04_O1() {
        // Root with one child: expect head0, head1, tail1, tail0
        RecordingVisitor v = new RecordingVisitor();
        Element root = new Element(Tag.valueOf("div"), "");
        Element child = new Element(Tag.valueOf("span"), "");
        root.appendChild(child);
        NodeTraversor.traverse(v, root);
        assertEquals(List.of("head0", "head1", "tail1", "tail0"), v.sequence);
    }

    @Test
    @DisplayName("TC05_O1: Visitor removes node in head causing skipped tail and correct ascent")
    public void test_TC05_O1() {
        // Single node removed in head -> head called once, tail never called
        RemovingVisitor v = new RemovingVisitor();
        Element root = new Element(Tag.valueOf("div"), "");
        NodeTraversor.traverse(v, root);
        assertEquals(1, v.headCount);
        assertEquals(0, v.tailCount);
    }

    @Test
    @DisplayName("TC06_O1: Visitor replaces node in head causing sibling index traversal")
    public void test_TC06_O1() {
        // Root with one child replaced in head -> replacement visited
        ReplacingVisitor v = new ReplacingVisitor();
        Element root = new Element(Tag.valueOf("div"), "");
        Element child = new Element(Tag.valueOf("span"), "");
        root.appendChild(child);
        NodeTraversor.traverse(v, root);
        // Replacement node at depth=1 gets head and tail
        assertTrue(v.sequence.contains("head1"));
        assertTrue(v.sequence.contains("tail1"));
    }

    @Test
    @DisplayName("TC07_O1: Deep tree of depth 3 exercises multiple descend/ascend loops")
    public void test_TC07_O1() {
        // Chain of 4 nodes yields 4 heads and 4 tails, maxDepth=3
        RecordingVisitor v = new RecordingVisitor();
        Element root = new Element(Tag.valueOf("div"), "");
        Element c1 = new Element(Tag.valueOf("a"), "");
        Element c2 = new Element(Tag.valueOf("b"), "");
        Element c3 = new Element(Tag.valueOf("c"), "");
        root.appendChild(c1);
        c1.appendChild(c2);
        c2.appendChild(c3);
        NodeTraversor.traverse(v, root);
        assertEquals(4, v.headCount + v.tailCount); // 4 head + 4 tail = 8 calls
        assertEquals(3, v.maxDepth);
    }

    @Test
    @DisplayName("TC08_O1: Node has no children but has next sibling exercises ascend loop")
    public void test_TC08_O1() {
        // Parent with two leaves: traverse siblings
        RecordingVisitor v = new RecordingVisitor();
        Element parent = new Element(Tag.valueOf("div"), "");
        Element leaf1 = new Element(Tag.valueOf("p"), "");
        Element leaf2 = new Element(Tag.valueOf("span"), "");
        parent.appendChild(leaf1);
        parent.appendChild(leaf2);
        NodeTraversor.traverse(v, parent);
        assertEquals(List.of("head0","tail0","head0","tail0"), v.sequence);
    }

    @Test
    @DisplayName("TC09_O2: Empty Elements list results in no traversal")
    public void test_TC09_O2() {
        // Empty Elements yields no calls
        RecordingVisitor v = new RecordingVisitor();
        Elements empty = new Elements(); // Corrected Elements initialization
        NodeTraversor.traverse(v, empty);
        assertEquals(0, v.headCount + v.tailCount);
    }

    @Test
    @DisplayName("TC10_O2: Single Element in Elements invokes single-node traversal")
    public void test_TC10_O2() {
        // One element with no children -> one head, one tail
        RecordingVisitor v = new RecordingVisitor();
        Element el = new Element(Tag.valueOf("div"), "");
        Elements list = new Elements(el); // Corrected Elements initialization
        NodeTraversor.traverse(v, list);
        assertEquals(1, v.headCount);
        assertEquals(1, v.tailCount);
    }
}