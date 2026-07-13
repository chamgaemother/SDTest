package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class Element_appendChild_2_Test {

    @Test
    @DisplayName("appendChild(reparenting) removes a non-last child from an old parent with multiple children and shifts siblingIndex of remaining")
    public void test_TC06() {
        // GIVEN oldParent with two children: first and second
        Element oldParent = new Element("div");
        TextNode first = new TextNode("first");
        TextNode second = new TextNode("second");
        oldParent.appendChild(first);   // first gets siblingIndex 0
        oldParent.appendChild(second);  // second gets siblingIndex 1
        Element newParent = new Element("section");

        // WHEN reparent first under newParent -> exercises reparent branch (B2) and existing childNodes non-empty (B3)
        newParent.appendChild(first);

        // THEN oldParent.childNodeSize() decrements by 1 (was 2, now 1)
        assertEquals(1, oldParent.childNodeSize(),
                "Old parent should have one child left after reparenting the first node");
        // remaining child's siblingIndex shifts down to 0
        assertEquals(0, second.siblingIndex(),
                "Remaining child in oldParent should have its siblingIndex updated to 0");

        // newParent now has one child
        assertEquals(1, newParent.childNodeSize(),
                "New parent should have one child after appendChild");
        // moved child's siblingIndex on newParent should be 0
        assertEquals(0, first.siblingIndex(),
                "Moved child should have siblingIndex 0 in newParent");
    }

    @Test
    @DisplayName("appendChild(Element) on a fresh parent allocates a new NodeList and sets siblingIndex on Element child")
    public void test_TC07() {
        // GIVEN parent with no children, and child an Element
        Element parent = new Element("ul");
        Element child = new Element("li");

        // WHEN append child -> triggers ensureChildNodes branch creating new NodeList (B3)
        Element result = parent.appendChild(child);

        // THEN parent.childNodeSize() becomes 1
        assertEquals(1, parent.childNodeSize(),
                "Parent should have exactly one child after appendChild on fresh parent");
        // child.siblingIndex() is set to 0
        assertEquals(0, child.siblingIndex(),
                "First child should have siblingIndex 0");
        // appendChild returns the parent element
        assertEquals(parent, result,
                "appendChild should return the parent for chaining");
    }
}