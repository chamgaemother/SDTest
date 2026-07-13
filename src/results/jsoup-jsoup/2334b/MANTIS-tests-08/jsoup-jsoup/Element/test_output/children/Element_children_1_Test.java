package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Element_children_1_Test {

    @Test
    @DisplayName("TC08 children() reuses cached element list when no mutation occurs (shadowChildrenRef non-null branch)")
    public void test_TC08() {
        // GIVEN an Element with one child Element
        Element el = new Element("div");
        Element span = new Element("span");
        el.appendChild(span);
        // WHEN: first call builds and caches the childElementsList
        Elements first = el.children(); // childNodeSize() > 0, shadowChildrenRef == null initially
        // THEN it has size 1
        assertEquals(1, first.size(), "first() should see the one appended <span>");
        // WHEN: second call reuses the cached list without any mutation
        Elements second = el.children(); // shadowChildrenRef.get() != null branch
        // THEN still size 1
        assertEquals(1, second.size(), "second() should reuse cache and still see one <span>");
        // Mutate the returned Elements list (should not clear the cache)
        first.remove(0);
        // THEN the underlying cached list is unaffected, so a new call still sees the original child
        Elements third = el.children();
        assertEquals(1, third.size(), "cache should not be invalidated by mutating the returned Elements");
        assertEquals("span", third.get(0).tagName());
    }

    @Test
    @DisplayName("TC09 children() rebuilds after removal mutation (nodelistChanged on removal branch)")
    public void test_TC09() throws Exception {
        // GIVEN an Element with two <li> children and the cache populated
        Element el = new Element("ul");
        Element a = new Element("li");
        Element b = new Element("li");
        el.appendChild(a);
        el.appendChild(b);
        // populate cache: childNodeSize()>0, shadowChildrenRef == null -> built
        Elements initial = el.children();
        assertEquals(2, initial.size(), "initial cache should contain both <li>s");
        // WHEN: remove one child from the underlying node list
        List<Node> nodes = el.childNodes();
        boolean removed = nodes.remove(a);
        assertTrue(removed, "childNodes().remove(a) should remove the first <li>");
        // reflectively invoke the package-private nodelistChanged() to clear the cache
        Method nodelistChanged = Element.class.getDeclaredMethod("nodelistChanged");
        nodelistChanged.setAccessible(true);
        nodelistChanged.invoke(el);
        // THEN children() should rebuild and return only the remaining child
        Elements result = el.children();
        assertEquals(1, result.size(), "after removal and cache clear, children() should rebuild to size 1");
        assertEquals("li", result.get(0).tagName(), "the remaining child should be the second <li>");
    }
}