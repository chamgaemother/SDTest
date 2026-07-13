package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
public class Element_children_2_Test {

    @Test
    @DisplayName("TC08 children() returns all Element children in insertion order when multiple Elements are present")
    public void test_TC08() {
        // GIVEN: a parent element with three child elements appended in order a, b, c
        Element parent = new Element("div");
        Element a = new Element("span");
        Element b = new Element("p");
        Element c = new Element("b");
        parent.appendChild(a);  // ensure childNodes -> non-empty triggers B3 path
        parent.appendChild(b);
        parent.appendChild(c);
        // WHEN: calling children() which filters only Element nodes preserving insertion order
        Elements out = parent.children();
        // THEN: expect size 3 and same references in order a, b, c
        assertEquals(3, out.size(), "Expected three child elements");
        assertSame(a, out.get(0), "First child should be 'a'");
        assertSame(b, out.get(1), "Second child should be 'b'");
        assertSame(c, out.get(2), "Third child should be 'c'");
    }

    @Test
    @DisplayName("TC09 children() caches the filtered child list so that subsequent calls without mutations return the same list instance")
    public void test_TC09() throws Exception {
        // GIVEN: an element with exactly one child element, no mutations after append
        Element el = new Element("ul");
        Element li = new Element("li");
        el.appendChild(li);  // ensures first creation of childElementsList cache
        // WHEN: invoke the package-private childElementsList() twice via reflection
        Method childElementsList = Element.class.getDeclaredMethod("childElementsList");
        childElementsList.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Element> list1 = (List<Element>) childElementsList.invoke(el);
        @SuppressWarnings("unchecked")
        List<Element> list2 = (List<Element>) childElementsList.invoke(el);
        // THEN: both calls should return the identical cached list instance without new allocation
        assertSame(list1, list2, "Expected the same cached list instance on repeated calls");
        assertEquals(1, list2.size(), "Expected exactly one element in the cached list");
        assertSame(li, list2.get(0), "Cached list should contain the appended child");
    }
}