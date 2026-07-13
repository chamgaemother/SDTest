package org.jsoup.select;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class NodeTraversor_traverse_1_Test {

    @Test
    @DisplayName("three-level nested nodes triggers inner-while ascend loop when leaf has no siblings")
    public void test_TC08() {
        // GIVEN a root element with a single child and a grandchild: triggers depth increments to 2
        Element root = new Element("div");
        Element child = new Element("p");
        Element grand = new Element("span");
        root.appendChild(child);
        child.appendChild(grand);
        RecordingVisitor visitor = new RecordingVisitor();

        // WHEN traversing the tree: will visit head at depths 0,1,2 then ascend without siblings
        NodeTraversor.traverse(visitor, root);

        // THEN head and tail calls must reflect proper depth-first order, including inner-while ascend
        List<String> expected = Arrays.asList(
                "head:div:0",   // enter root at depth 0
                "head:p:1",     // descend to child at depth 1
                "head:span:2",  // descend to grandchild at depth 2
                "tail:span:2",  // ascend inner-while since span has no siblings
                "tail:p:1",     // then tail on child
                "tail:div:0"    // finally tail on root
        );
        assertEquals(expected, visitor.getCalls());
    }

    /**
     * A simple NodeVisitor that records head and tail calls.
     */
    static class RecordingVisitor implements NodeVisitor {
        private final List<String> calls = new ArrayList<>();

        @Override
        public void head(Node node, int depth) {
            calls.add("head:" + node.nodeName() + ":" + depth);
        }

        @Override
        public void tail(Node node, int depth) {
            calls.add("tail:" + node.nodeName() + ":" + depth);
        }

        public List<String> getCalls() {
            return calls;
        }
    }
}