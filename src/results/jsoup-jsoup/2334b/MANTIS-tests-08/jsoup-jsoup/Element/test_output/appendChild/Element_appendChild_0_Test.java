package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import java.lang.reflect.Field;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
public class Element_appendChild_0_Test {

    @Test
    @DisplayName("TC01: appendChild on new element with no children triggers ensureChildNodes branch and adds child at index 0")
    void test_TC01() throws Exception {
        // GIVEN: a fresh Element with childNodes == EmptyNodes so ensureChildNodes() must run (branch-true)
        Element el = new Element("div");
        TextNode child = new TextNode("x");
        
        // WHEN
        el.appendChild(child);
        
        // THEN: childNodes list size 1 and element at 0 must be our child
        Field childNodesField = Element.class.getDeclaredField("childNodes");
        childNodesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<?> nodes = (List<?>) childNodesField.get(el);
        assertEquals(1, nodes.size(), "after appendChild on empty, size should be 1");
        assertSame(child, nodes.get(0), "the only child must be the appended node");

        // verify parent() and siblingIndex()
        assertSame(el, child.parent(), "child.parent should be the element");
        Field siblingIdx = TextNode.class.getSuperclass().getDeclaredField("siblingIndex");
        siblingIdx.setAccessible(true);
        int idx = siblingIdx.getInt(child);
        assertEquals(0, idx, "siblingIndex should be 0 for first child");
    }

    @Test
    @DisplayName("TC02: appendChild on element with existing children skips ensureChildNodes and adds child at next index")
    void test_TC02() throws Exception {
        // GIVEN: element already has one child so ensureChildNodes() short-circuits (branch-false)
        Element el = new Element("span");
        TextNode a = new TextNode("a");
        el.appendChild(a);
        TextNode b = new TextNode("b");
        
        // WHEN
        el.appendChild(b);
        
        // THEN: childNodes size becomes 2 and second element is b
        Field childNodesField = Element.class.getDeclaredField("childNodes");
        childNodesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<?> nodes = (List<?>) childNodesField.get(el);
        assertEquals(2, nodes.size(), "after second appendChild, size should be 2");
        assertSame(b, nodes.get(1), "second child must be the newly appended node");

        // verify parent() and siblingIndex()
        assertSame(el, b.parent(), "child.parent should be the element");
        Field siblingIdx = TextNode.class.getSuperclass().getDeclaredField("siblingIndex");
        siblingIdx.setAccessible(true);
        int idx = siblingIdx.getInt(b);
        assertEquals(1, idx, "siblingIndex should be 1 for second child");
    }

    @Test
    @DisplayName("TC03: appendChild reassigns child from old parent to new parent and updates siblingIndex")
    void test_TC03() throws Exception {
        // GIVEN: child is already appended to oldParent (branch-true for ensureChildNodes on both), then reparented
        Element oldParent = new Element("p");
        Element newParent = new Element("div");
        TextNode child = new TextNode("c");
        oldParent.appendChild(child);
        
        // WHEN: reparent the same child to newParent
        newParent.appendChild(child);
        
        // THEN: oldParent should lose its child
        Field childNodesField = Element.class.getDeclaredField("childNodes");
        childNodesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<?> oldNodes = (List<?>) childNodesField.get(oldParent);
        assertEquals(0, oldNodes.size(), "old parent should have no children after reparenting");

        // newParent should have size 1 and child at 0
        @SuppressWarnings("unchecked")
        List<?> newNodes = (List<?>) childNodesField.get(newParent);
        assertEquals(1, newNodes.size(), "new parent should have one child");
        assertSame(child, newNodes.get(0), "child should be present in new parent");

        // verify new parent is set, and siblingIndex reset to 0
        assertSame(newParent, child.parent(), "child.parent should be the new parent");
        Field siblingIdx = TextNode.class.getSuperclass().getDeclaredField("siblingIndex");
        siblingIdx.setAccessible(true);
        int idx = siblingIdx.getInt(child);
        assertEquals(0, idx, "siblingIndex should be reset to 0 after reparenting");
    }

    @Test
    @DisplayName("TC04: appendChild with null child triggers validation exception")
    void test_TC04() {
        // GIVEN: an element
        Element el = new Element("div");
        
        // WHEN & THEN: passing null should throw IllegalArgumentException (Validate.notNull)
        assertThrows(IllegalArgumentException.class, () -> el.appendChild(null));
    }
}