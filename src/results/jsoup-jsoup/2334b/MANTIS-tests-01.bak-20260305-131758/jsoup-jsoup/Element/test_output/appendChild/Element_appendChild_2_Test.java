package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;
public class Element_appendChild_2_Test {

    @Test
    @DisplayName("Appending the same child twice on a parent repositions it without duplication (reparentChild branch when child already in parent)")
    void test_TC05() throws Exception {
        // GIVEN: a parent with one TextNode child already attached
        Element parent = new Element("div");
        TextNode child = new TextNode("dup");
        parent.appendChild(child);
        assertEquals(1, parent.childNodeSize(), "Precondition: parent should have exactly one child");

        // WHEN: appending the same child again should trigger reparentChild logic branch
        Element result = parent.appendChild(child);

        // THEN: method should return the same parent
        assertSame(parent, result, "appendChild should return the parent for chaining");
        // child list size remains 1 (no duplication)
        assertEquals(1, parent.childNodeSize(), "Appending existing child should not duplicate it");
        // child's parent remains set correctly
        assertSame(parent, child.parent(), "Child's parent should still be the parent element");
        // reflection: siblingIndex field should be reset to 0 after reparent
        Field sibIndexField = TextNode.class.getSuperclass() /* Node */
                .getDeclaredField("siblingIndex");
        sibIndexField.setAccessible(true);
        int siblingIndex = sibIndexField.getInt(child);
        assertEquals(0, siblingIndex, "Sibling index of the re-appended child should reset to 0");
    }

    @Test
    @DisplayName("Appending a non-Text Node subtype (Element) ensures reparent and correct indexing")
    void test_TC06() throws Exception {
        // GIVEN: a fresh parent and a fresh Element child not yet attached
        Element parent = new Element("section");
        Element child = new Element("span");

        // WHEN: appendChild on a new Element triggers ensureChildNodes and add branch
        Element result = parent.appendChild(child);

        // THEN: method returns parent for chaining
        assertSame(parent, result, "appendChild should return the parent for chaining");
        // parent now has one child
        assertEquals(1, parent.childNodeSize(), "Parent should have exactly one child after append");
        // child's parent should be set to parent
        assertSame(parent, child.parent(), "Child's parent should be set to the parent element");
        // reflection: ensure siblingIndex set to 0 on first append
        Field sibIndexField = Element.class.getSuperclass() /* Node */
                .getDeclaredField("siblingIndex");
        sibIndexField.setAccessible(true);
        int siblingIndex = sibIndexField.getInt(child);
        assertEquals(0, siblingIndex, "Sibling index of the first appended child should be 0");
    }
}