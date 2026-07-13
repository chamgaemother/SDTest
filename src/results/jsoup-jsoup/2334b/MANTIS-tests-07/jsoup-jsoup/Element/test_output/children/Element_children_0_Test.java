package org.jsoup.nodes;

import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.WeakReference;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;
public class Element_children_0_Test {

    @Test
    @DisplayName("TC01: children() on new Element with no child nodes returns empty list (childNodeSize==0)")
    public void test_TC01() {
        org.jsoup.nodes.Element el = new org.jsoup.nodes.Element("div");
        Elements result = el.children();
        assertEquals(0, result.size(), "Expected no child elements for a new element");
    }

    @Test
    @DisplayName("TC02: children() on Element with one TextNode returns empty list after one filter iteration")
    public void test_TC02() {
        org.jsoup.nodes.Element el = new org.jsoup.nodes.Element("p");
        el.appendText("hello");
        Elements result = el.children();
        assertEquals(0, result.size(), "Text nodes should not be returned as children()");
    }

    @Test
    @DisplayName("TC03: children() on Element with one child Element returns list of size 1")
    public void test_TC03() {
        org.jsoup.nodes.Element parent = new org.jsoup.nodes.Element("ul");
        org.jsoup.nodes.Element li = new org.jsoup.nodes.Element("li");
        parent.appendChild(li);
        Elements result = parent.children();
        assertAll(
            () -> assertEquals(1, result.size(), "Expected one child element"),
            () -> assertSame(li, result.get(0), "Returned element should be same instance as appended child")
        );
    }

    @Test
    @DisplayName("TC04: children() on Element with two Element children returns both in order")
    public void test_TC04() {
        org.jsoup.nodes.Element parent = new org.jsoup.nodes.Element("div");
        org.jsoup.nodes.Element a = new org.jsoup.nodes.Element("a");
        org.jsoup.nodes.Element b = new org.jsoup.nodes.Element("b");
        parent.appendChild(a);
        parent.appendChild(b);
        Elements result = parent.children();
        assertEquals(2, result.size(), "Expected two child elements");
        assertSame(a, result.get(0), "First child should be 'a'");
        assertSame(b, result.get(1), "Second child should be 'b'");
    }

    @Test
    @DisplayName("TC05: children() on Element with mixed Node types returns only Element children")
    public void test_TC05() {
        org.jsoup.nodes.Element parent = new org.jsoup.nodes.Element("p");
        parent.appendText("one");
        org.jsoup.nodes.Element span = new org.jsoup.nodes.Element("span");
        parent.appendChild(span);
        parent.appendText("two");
        Elements result = parent.children();
        assertEquals(1, result.size(), "Only the span element should be returned");
        assertSame(span, result.get(0), "Returned element should be the span instance");
    }

    @Test
    @DisplayName("TC06: children() sets cache on first invocation (shadowChildrenRef null then non-null)")
    public void test_TC06() throws Exception {
        org.jsoup.nodes.Element el = new org.jsoup.nodes.Element("div");
        Field f = org.jsoup.nodes.Element.class.getDeclaredField("shadowChildrenRef");
        f.setAccessible(true);
        assertNull(f.get(el), "shadowChildrenRef should initially be null");
        el.children();
        @SuppressWarnings("unchecked")
        WeakReference<List<org.jsoup.nodes.Element>> ref = (WeakReference<List<org.jsoup.nodes.Element>>) f.get(el);
        assertNotNull(ref, "shadowChildrenRef should be non-null after children()");
        assertNotNull(ref.get(), "Cached child list should not be null");
    }

    @Test
    @DisplayName("TC07: children() reuses cached list when shadowChildrenRef non-null and childNodes unchanged")
    public void test_TC07() throws Exception {
        org.jsoup.nodes.Element el = new org.jsoup.nodes.Element("div");
        org.jsoup.nodes.Element a = new org.jsoup.nodes.Element("a");
        el.appendChild(a);
        Elements first = el.children();
        Elements second = el.children();
        assertSame(first, second, "children() should return same cached list when no mutation has occurred");
    }

    @Test
    @DisplayName("TC08: children() invalidates cache after appendChild triggers nodelistChanged")
    public void test_TC08() throws Exception {
        org.jsoup.nodes.Element el = new org.jsoup.nodes.Element("div");
        org.jsoup.nodes.Element a = new org.jsoup.nodes.Element("a");
        el.appendChild(a);
        el.children(); // populate cache
        Field f = org.jsoup.nodes.Element.class.getDeclaredField("shadowChildrenRef");
        f.setAccessible(true);
        WeakReference<?> before = (WeakReference<?>) f.get(el);
        assertNotNull(before, "Cache should exist before mutation");
        org.jsoup.nodes.Element b = new org.jsoup.nodes.Element("b");
        el.appendChild(b); // triggers nodelistChanged, clearing cache
        WeakReference<List<org.jsoup.nodes.Element>> after = (WeakReference<List<org.jsoup.nodes.Element>>) f.get(el);
        assertNull(after, "Cache should be invalidated (null) after appendChild");
    }

    @Test
    @DisplayName("TC09: children() returned list modification does not affect underlying childNodes")
    public void test_TC09() {
        org.jsoup.nodes.Element parent = new org.jsoup.nodes.Element("div");
        org.jsoup.nodes.Element e1 = new org.jsoup.nodes.Element("e1");
        org.jsoup.nodes.Element e2 = new org.jsoup.nodes.Element("e2");
        parent.appendChild(e1);
        parent.appendChild(e2);
        Elements result = parent.children();
        result.clear();
        assertEquals(2, parent.childNodeSize(), "Modifying children() result should not change underlying childNodes");
    }

    @Test
    @DisplayName("TC10: children() on Element with ten child Elements returns all")
    public void test_TC10() {
        org.jsoup.nodes.Element parent = new org.jsoup.nodes.Element("div");
        IntStream.range(0, 10).forEach(i -> parent.appendChild(new org.jsoup.nodes.Element("li")));
        Elements result = parent.children();
        assertEquals(10, result.size(), "Expected ten child elements");
        assertTrue(result.stream().allMatch(el -> el instanceof org.jsoup.nodes.Element), "All returned items should be instances of Element");
    }
}