package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.Node;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for Element.appendChild, covering reparenting into a non-empty parent (ensureChildNodes=false branch).
 */
public class Element_appendChild_1_Test {

    @Test
    @DisplayName("reparent a node into a parent whose childNodes is already non-empty (ensureChildNodes branch false)")
    void test_TC05() throws Exception {
        // GIVEN: a new parent with one existing child to ensure childNodes != EmptyNodes (avoid creating a new list)
        Element oldParent = new Element("div");
        Element newParent = new Element("section");
        TextNode existing = new TextNode("existing");
        newParent.appendChild(existing);
        // GIVEN: a child already attached to oldParent
        TextNode child = new TextNode("moved");
        oldParent.appendChild(child);

        // WHEN: reparent child into newParent where childNodes is non-empty -> ensureChildNodes() returns existing list (false branch)
        Element result = newParent.appendChild(child);

        // THEN: oldParent should have no children, newParent should have two, child parent updated, siblingIndex = 1, and returned element is newParent
        assertAll("Verify reparenting behavior",
            () -> assertEquals(0, oldParent.childNodeSize(),
                    "Old parent should have no child nodes after reparenting"),
            () -> assertEquals(2, newParent.childNodeSize(),
                    "New parent should have two child nodes after reparenting"),
            () -> assertEquals(newParent, child.parent(),
                    "Child's parent should be updated to newParent"),
            () -> {
                // Access private siblingIndex field from Node via reflection
                Field siblingField = Node.class.getDeclaredField("siblingIndex");
                siblingField.setAccessible(true);
                int idx = siblingField.getInt(child);
                assertEquals(1, idx,
                    "Sibling index should be 1 (second position) for the moved child");
            },
            () -> assertEquals(newParent, result,
                    "appendChild should return the newParent for chaining")
        );
    }
}