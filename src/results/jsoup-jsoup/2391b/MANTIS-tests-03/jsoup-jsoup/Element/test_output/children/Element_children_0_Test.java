package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.Comment;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Attributes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
public class Element_children_0_Test {

    @Test
    @DisplayName("TC01: childNodeSize == 0 returns empty Elements (loop-0)")
    void test_TC01() {
        // GIVEN an element with no children (childNodeSize == 0 triggers early return)
        Element el = new Element("div");
        // WHEN
        Elements result = el.children();
        // THEN expect empty list
        assertEquals(0, result.size(), "Expected no child elements when none appended");
    }

    @Test
    @DisplayName("TC02: childNodes contain only TextNode => children returns empty (filter none)")
    void test_TC02() {
        // GIVEN an element with a single TextNode child (loop once, but no Element matches)
        Element el = new Element("p");
        el.appendChild(new TextNode("text"));
        // WHEN
        Elements result = el.children();
        // THEN expect empty list since TextNode is filtered out
        assertEquals(0, result.size(), "Expected no element children when only text nodes present");
    }

    @Test
    @DisplayName("TC03: single child Element returns list of size 1 (loop-1-match)")
    void test_TC03() {
        // GIVEN an element with one Element child (loop once, match at first iteration)
        Element parent = new Element("div");
        Element child = new Element("span");
        parent.appendChild(child);
        // WHEN
        Elements result = parent.children();
        // THEN
        assertEquals(1, result.size(), "Expected one child element");
        assertSame(child, result.get(0), "Expected returned child to be exactly the appended instance");
    }

    @Test
    @DisplayName("TC04: multiple child Elements returns all in order (loop-N-match)")
    void test_TC04() {
        // GIVEN an element with three li children (loop three times, match each)
        Element el = new Element("ul");
        Element li1 = new Element("li");
        Element li2 = new Element("li");
        Element li3 = new Element("li");
        el.appendChild(li1);
        el.appendChild(li2);
        el.appendChild(li3);
        // WHEN
        Elements result = el.children();
        // THEN
        assertEquals(3, result.size(), "Expected three child elements");
        assertSame(li1, result.get(0), "First child should be li1");
        assertSame(li2, result.get(1), "Second child should be li2");
        assertSame(li3, result.get(2), "Third child should be li3");
    }

    @Test
    @DisplayName("TC05: mixed child nodes filters non-Element and Element correctly (loop-N-filter)")
    void test_TC05() {
        // GIVEN an element with mixed node types: TextNode, Element(a), Comment, Element(b)
        // filter should skip non-Element and include only a and b
        Element el = new Element("div");
        el.appendChild(new TextNode("t"));
        Element a = new Element("a");
        el.appendChild(a);
        el.appendChild(new Comment("c"));
        Element b = new Element("b");
        el.appendChild(b);
        // WHEN
        Elements result = el.children();
        // THEN
        assertEquals(2, result.size(), "Expected two element children after filtering");
        assertSame(a, result.get(0), "First filtered element should be 'a'");
        assertSame(b, result.get(1), "Second filtered element should be 'b'");
    }

    @Test
    @DisplayName("TC06: caching path: cachedChildren returns pre-cached list (cache-hit)")
    void test_TC06() throws Exception {
        // GIVEN an element with one span child and a valid cache hit
        Element el = new Element("div");
        Element span = new Element("span");
        el.appendChild(span);
        Attributes attrs = el.attributes();
        Map<String,Object> ud = attrs.userData();
        List<Element> cached = Collections.unmodifiableList(Arrays.asList(span));
        // place matching WeakReference and modCount
        ud.put("jsoup.childEls", new WeakReference<>(cached));
        ud.put("jsoup.childElsMod", el.childNodes.modCount());
        // WHEN
        Elements result = el.children();
        // THEN inspect private field in Elements to verify cache used
        Field listField = Elements.class.getDeclaredField("elements");
        listField.setAccessible(true);
        Object inner = listField.get(result);
        assertSame(cached, inner, "Expected to reuse cached list instance on cache hit");
    }

    @Test
    @DisplayName("TC07: cache-miss due to missing userData reference results in fresh list")
    void test_TC07() {
        // GIVEN an element with one <p> child and a cache entry whose WeakReference yields null
        Element el = new Element("div");
        Element p = new Element("p");
        el.appendChild(p);
        Attributes attrs = el.attributes();
        Map<String,Object> ud = attrs.userData();
        ud.put("jsoup.childEls", new WeakReference<List<Element>>(null));
        ud.put("jsoup.childElsMod", el.childNodes.modCount());
        // WHEN
        Elements result = el.children();
        // THEN fresh list, size==1, and contains our element
        assertEquals(1, result.size(), "Expected one child element on cache miss");
        assertSame(p, result.get(0), "Expected the only element to be our appended <p>");
    }

    @Test
    @DisplayName("TC08: cache-miss due to modCount mismatch refreshes list")
    void test_TC08() {
        // GIVEN an element with one span child and stale modCount
        Element el = new Element("div");
        Element span = new Element("span");
        el.appendChild(span);
        Attributes attrs = el.attributes();
        Map<String,Object> ud = attrs.userData();
        List<Element> cached = Collections.unmodifiableList(Arrays.asList(span));
        ud.put("jsoup.childEls", new WeakReference<>(cached));
        ud.put("jsoup.childElsMod", el.childNodes.modCount() + 1); // stale
        // WHEN
        Elements result = el.children();
        // THEN new list instance
        assertEquals(1, result.size(), "Expected one child element despite stale cache");
        // ensure not using stale cached object
        assertNotSame(cached, result, "Expected a new list instance due to modCount mismatch");
        assertSame(span, result.get(0), "Still should contain the span element");
    }

    @Test
    @DisplayName("TC09: large number of children N=10 performance path (loop-N-large)")
    void test_TC09() {
        // GIVEN an element with 10 children (loop ten times)
        Element el = new Element("div");
        for (int i = 0; i < 10; i++) {
            el.appendChild(new Element("i" + i));
        }
        // WHEN
        Elements result = el.children();
        // THEN
        assertEquals(10, result.size(), "Expected ten element children for N=10");
        // optional: verify first and last tag names
        assertEquals("i0", result.get(0).tagName(), "First tag should be i0");
        assertEquals("i9", result.get(9).tagName(), "Last tag should be i9");
    }

    @Test
    @DisplayName("TC10: concurrent modification invalidates cache and refreshes (loop-2)")
    void test_TC10() throws Exception {
        // GIVEN an element with one <a> child, valid cache, then modification to childNodes
        Element el = new Element("div");
        Element a = new Element("a");
        el.appendChild(a);
        Attributes attrs = el.attributes();
        Map<String,Object> ud = attrs.userData();
        List<Element> cached = Collections.unmodifiableList(Arrays.asList(a));
        ud.put("jsoup.childEls", new WeakReference<>(cached));
        ud.put("jsoup.childElsMod", el.childNodes.modCount());
        // concurrent modification: append new <b>
        Element b = new Element("b");
        el.appendChild(b);
        // WHEN
        Elements result = el.children();
        // THEN list refreshed to include both
        assertEquals(2, result.size(), "Expected two children after modification invalidates cache");
        assertEquals("a", result.get(0).tagName(), "First child should remain 'a'");
        assertEquals("b", result.get(1).tagName(), "Second child should be newly added 'b'");
    }
}