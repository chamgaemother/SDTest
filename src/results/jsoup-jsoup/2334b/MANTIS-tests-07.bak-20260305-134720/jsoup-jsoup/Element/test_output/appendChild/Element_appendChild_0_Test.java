package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;
public class Element_appendChild_0_Test {

    @Test
    @DisplayName("appendChild(null) throws IllegalArgumentException for null child parameter")
    public void test_TC01() {
        Element el = new Element("div");
        // Passing null should hit Validate.notNull(child) and throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> el.appendChild(null));
    }

    @Test
    @DisplayName("appendChild(child) on fresh element initializes childNodes and appends one child")
    public void test_TC02() throws Exception {
        Element el = new Element("div");
        Element child = new Element("span");
        // childNodes was initially EmptyNodes, so ensureChildNodes() path is taken (branch B3_true)
        Element returned = el.appendChild(child);
        // should return same instance
        assertSame(el, returned);
        // one child added
        assertEquals(1, el.childNodeSize());
        // child's parent should be set to el
        assertSame(el, child.parent());
        // child's siblingIndex should be 0
        Field sibField = Node.class.getDeclaredField("siblingIndex");
        sibField.setAccessible(true);
        int idx = sibField.getInt(child);
        assertEquals(0, idx);
    }

    @Test
    @DisplayName("appendChild(child) on element with existing children appends second child without reinitializing childNodes")
    public void test_TC03() throws Exception {
        Element el = new Element("div");
        Element first = new Element("p");
        el.appendChild(first);
        Element second = new Element("span");
        // childNodes is no longer EmptyNodes, so ensureChildNodes() will not reinitialize (branch B3_false)
        Element returned = el.appendChild(second);
        assertSame(el, returned);
        assertEquals(2, el.childNodeSize());
        // second parent's set correctly
        assertSame(el, second.parent());
        // second siblingIndex should be 1
        Field sibField = Node.class.getDeclaredField("siblingIndex");
        sibField.setAccessible(true);
        int idx2 = sibField.getInt(second);
        assertEquals(1, idx2);
    }
}