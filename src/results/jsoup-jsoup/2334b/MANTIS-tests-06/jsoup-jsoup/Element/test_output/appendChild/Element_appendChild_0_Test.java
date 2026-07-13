package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Element_appendChild_0_Test {

    @Test
    @DisplayName("appendChild(null) throws NullPointerException for null input")
    void test_TC01() throws Exception {
        // GIVEN an Element with no children
        Element el = new Element("div");
        // use reflection to get initial childNodes list
        Field childrenField = Element.class.getDeclaredField("childNodes");
        childrenField.setAccessible(true);
        Object initialList = childrenField.get(el);
        // WHEN appendChild is called with null
        NullPointerException ex = assertThrows(NullPointerException.class, () -> {
            el.appendChild(null);
        }, "Expected appendChild(null) to throw NPE"); 
        // THEN the childNodes field should not have been modified
        Object afterList = childrenField.get(el);
        assertSame(initialList, afterList,
                "childNodes list reference should remain unchanged after exception");
    }

    @Test
    @DisplayName("appendChild adds first child when childNodes is empty (ensureChildNodes path)")
    void test_TC02() throws Exception {
        // GIVEN an Element with no children
        Element parent = new Element("div");
        TextNode child = new TextNode("text");
        // reflectively inspect childNodes before call
        Field childrenField = Element.class.getDeclaredField("childNodes");
        childrenField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<?> before = (List<?>) childrenField.get(parent);
        assertTrue(before.isEmpty(), "Precondition: no existing childNodes");

        // WHEN appendChild is called with a new TextNode
        parent.appendChild(child);
        // THEN there should be exactly one child in childNodes
        @SuppressWarnings("unchecked")
        List<?> after = (List<?>) childrenField.get(parent);
        assertEquals(1, after.size(), "childNodes.size()==1 after first appendChild");
        assertSame(child, after.get(0), "The only element should be the appended child");

        // also check siblingIndex field on child via reflection
        Field idxField = TextNode.class.getSuperclass().getDeclaredField("siblingIndex");
        idxField.setAccessible(true);
        int idx = idxField.getInt(child);
        assertEquals(0, idx, "New child's siblingIndex should be 0 for first child");
    }

    @Test
    @DisplayName("appendChild adds second child when childNodes already contains one child")
    void test_TC03() throws Exception {
        // GIVEN an Element already containing one child
        Element parent = new Element("div");
        TextNode first = new TextNode("one");
        parent.appendChild(first);
        // inline comment: after first append, childNodes != EmptyNodes, ensureChildNodes skipped branch
        TextNode second = new TextNode("two");

        // WHEN appendChild is called again
        parent.appendChild(second);

        // THEN childNodes has two entries and second is at index 1
        Field childrenField = Element.class.getDeclaredField("childNodes");
        childrenField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<?> list = (List<?>) childrenField.get(parent);
        assertEquals(2, list.size(), "childNodes.size()==2 after second appendChild");
        assertSame(second, list.get(1), "Second appended child at index 1");

        // and new child's siblingIndex should be 1
        Field idxField = TextNode.class.getSuperclass().getDeclaredField("siblingIndex");
        idxField.setAccessible(true);
        int idx = idxField.getInt(second);
        assertEquals(1, idx, "New child's siblingIndex should be 1 for second child");
    }
}