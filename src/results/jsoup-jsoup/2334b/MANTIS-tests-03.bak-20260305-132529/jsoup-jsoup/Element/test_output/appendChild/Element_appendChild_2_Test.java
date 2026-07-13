package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
public class Element_appendChild_2_Test {
    @Test
    @DisplayName("appendChild moves an existing node into a non-empty new parent reusing its childNodes (ensureChildNodes=false)")
    public void test_TC06() {
        // GIVEN: oldParent has initialized childNodes via appendChild of its first child
        Element oldParent = new Element("div");
        TextNode child = new TextNode("c");
        oldParent.appendChild(child); // oldParent.childNodes now allocated (non-empty)
        
        // GIVEN: newParent has one TextNode child, so its childNodes allocated
        Element newParent = new Element("section");
        newParent.appendChild(new TextNode("first")); // newParent.childNodes != EmptyNodes
        
        // WHEN: appendChild moves existing child into newParent, ensureChildNodes sees non-empty childNodes and reuses it (B4→B5→B7 branch)
        newParent.appendChild(child);
        
        // THEN: child removed from oldParent and added at index 1 in newParent
        assertAll("Move child between parents",
            () -> assertEquals(0, oldParent.childNodeSize(), "oldParent should have no child after move"),
            () -> assertEquals(2, newParent.childNodeSize(), "newParent should have two children"),
            () -> assertSame(newParent, child.parent(), "child's parent should be newParent"),
            () -> assertEquals(1, child.siblingIndex(), "child's sibling index should be 1 in newParent")
        );
    }

    @Test
    @DisplayName("appendChild of a node without an existing parent into an empty parent returns correct chaining and index")
    public void test_TC07() {
        // GIVEN: parent has no children yet, so childNodes == EmptyNodes
        Element parent = new Element("article");
        Element child = new Element("section"); // fresh element with no parent
        
        // WHEN: appendChild on empty parent (B1→B3→B5→B7 path), childNodes was empty so ensureChildNodes allocates new list
        Element returned = parent.appendChild(child);
        
        // THEN: returns parent itself and siblingIndex set to 0
        assertAll("Chaining and sibling index",
            () -> assertSame(parent, returned, "appendChild should return the parent instance"),
            () -> assertEquals(1, parent.childNodeSize(), "parent should have exactly one child"),
            () -> assertEquals(0, child.siblingIndex(), "child's sibling index should be 0 in new parent")
        );
    }
}