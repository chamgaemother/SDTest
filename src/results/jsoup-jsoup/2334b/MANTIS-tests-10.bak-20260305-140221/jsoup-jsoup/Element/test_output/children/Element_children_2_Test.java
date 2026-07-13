package org.jsoup.nodes;

import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Element_children_2_Test {
    @Test
    @DisplayName("childElementsList() returns and caches a new list of element children on first invocation (shadowChildrenRef==null branch)")
    public void test_TC04() throws Exception {
        // GIVEN an element with two element children to trigger size>0 and null cache
        Element el = new Element("div");
        Element c1 = new Element("p"); el.appendChild(c1);
        Element c2 = new Element("span"); el.appendChild(c2);
        // prepare reflection access to private childElementsList()
        Method m = Element.class.getDeclaredMethod("childElementsList");
        m.setAccessible(true);
        // WHEN first invocation builds and caches list
        @SuppressWarnings("unchecked")
        List<Element> list = (List<Element>) m.invoke(el);
        // THEN list contains exactly c1 and c2 in order, verifying build loop
        assertEquals(2, list.size(), "Expected two child elements in list");
        assertSame(c1, list.get(0), "First element should be c1");
        assertSame(c2, list.get(1), "Second element should be c2");
        // AND the weak reference field stores the same list as referent
        Field refField = Element.class.getDeclaredField("shadowChildrenRef");
        refField.setAccessible(true);
        @SuppressWarnings("unchecked")
        WeakReference<List<Element>> ref = (WeakReference<List<Element>>) refField.get(el);
        assertNotNull(ref, "shadowChildrenRef should be set");
        assertSame(list, ref.get(), "WeakReference referent should be the cached list");
    }

    @Test
    @DisplayName("childElementsList() rebuilds list after structural mutation clears cache via nodelistChanged()")
    public void test_TC05() throws Exception {
        // GIVEN an element with one child to build initial cache
        Element el = new Element("div");
        Element c1 = new Element("p"); el.appendChild(c1);
        Method m = Element.class.getDeclaredMethod("childElementsList");
        m.setAccessible(true);
        @SuppressWarnings("unchecked") List<Element> first = (List<Element>) m.invoke(el);
        // WHEN structural mutation occurs: prependChild should clear cache via nodelistChanged
        el.prependChild(new Element("span"));
        @SuppressWarnings("unchecked") List<Element> second = (List<Element>) m.invoke(el);
        // THEN the cache is rebuilt: new list instance, size is now 2
        assertNotSame(first, second, "Cache should have been rebuilt after mutation");
        assertEquals(2, second.size(), "After prepending, there should be two elements");
    }

    @Test
    @DisplayName("childElementsList() returns same cached list after a non-structural attribute change does not clear cache")
    public void test_TC06() throws Exception {
        // GIVEN a parent with two children and initial cache built
        Element parent = new Element("div");
        Element c1 = new Element("a"); parent.appendChild(c1);
        Element c2 = new Element("b"); parent.appendChild(c2);
        Method m = Element.class.getDeclaredMethod("childElementsList");
        m.setAccessible(true);
        @SuppressWarnings("unchecked") List<Element> first = (List<Element>) m.invoke(parent);
        // WHEN non-structural change: setting an attribute should not clear the child cache
        parent.attr("class", "x");
        @SuppressWarnings("unchecked") List<Element> second = (List<Element>) m.invoke(parent);
        // THEN cached list instance is reused and still contains two elements
        assertSame(first, second, "Cache should be preserved after attribute mutation");
        assertEquals(2, second.size(), "Cached list should still have two elements");
    }

    @Test
    @DisplayName("childElementsList() rebuilds cache when weak referent is cleared (simulated GC)")
    public void test_TC07() throws Exception {
        // GIVEN a parent with three children and initial cache built
        Element parent = new Element("div");
        Element a = new Element("a"); parent.appendChild(a);
        Element b = new Element("b"); parent.appendChild(b);
        Element c = new Element("c"); parent.appendChild(c);
        Method m = Element.class.getDeclaredMethod("childElementsList");
        m.setAccessible(true);
        @SuppressWarnings("unchecked") List<Element> first = (List<Element>) m.invoke(parent);
        // simulate GC: clear weak referent by resetting shadowChildrenRef to a cleared WeakReference
        Field refField = Element.class.getDeclaredField("shadowChildrenRef");
        refField.setAccessible(true);
        refField.set(parent, new WeakReference<List<Element>>(null));
        // WHEN invoking again should rebuild cache due to null referent
        @SuppressWarnings("unchecked") List<Element> second = (List<Element>) m.invoke(parent);
        // THEN the cache is rebuilt: new list instance with three children
        assertNotSame(first, second, "Cache should be rebuilt when weak referent is null");
        assertEquals(3, second.size(), "Rebuilt list should contain three elements");
    }

    @Test
    @DisplayName("children() returns an empty Elements even if only non-Element DataNode children present")
    public void test_TC08() {
        // GIVEN an element with a data node child via script fragment, childNodeSize>0 but no Element children
        Element el = new Element("div");
        el.append("<script>raw</script>");
        // WHEN children() filters and finds no Element instances
        Elements result = el.children();
        // THEN result is empty
        assertTrue(result.isEmpty(), "children() should return empty when only DataNode children present");
    }
}