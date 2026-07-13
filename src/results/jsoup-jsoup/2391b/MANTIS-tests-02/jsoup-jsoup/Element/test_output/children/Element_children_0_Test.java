package org.jsoup.nodes;

import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Element_children_0_Test {

    @Test
    @DisplayName("children() on new element with no children returns empty Elements (childNodeSize == 0)")
    void test_TC01() {
        // No children: childNodeSize == 0, should return empty collection
        org.jsoup.nodes.Element e = new org.jsoup.nodes.Element("div");
        Elements result = e.children();
        assertNotNull(result, "children() should not return null");
        assertEquals(0, result.size(), "Expected no child elements for a new div");
    }

    @Test
    @DisplayName("children() on element with only non-Element children returns empty Elements (filter yields none)")
    void test_TC02() {
        // Only TextNode child: loop sees nodes but filters out non-Element, so result size == 0
        org.jsoup.nodes.Element e = new org.jsoup.nodes.Element("div");
        e.appendText("hello");
        Elements result = e.children();
        assertEquals(0, result.size(), "Expected no element children when only text nodes present");
    }

    @Test
    @DisplayName("children() on element with one Element child returns list of size 1 (loop-1)")
    void test_TC03() {
        // Single Element child: loop should include exactly one element
        org.jsoup.nodes.Element parent = new org.jsoup.nodes.Element("div");
        org.jsoup.nodes.Element child = parent.appendElement("span");
        Elements result = parent.children();
        assertAll("Check single child element",
            () -> assertEquals(1, result.size(), "Expected exactly one child element"),
            () -> assertSame(child, result.get(0), "Child element should be the same instance appended")
        );
    }

    @Test
    @DisplayName("children() with mixed Element and non-Element children returns only Element nodes (loop-N)")
    void test_TC04() {
        // Mixed nodes: text, element, text, element; loop should collect only the two element children in order
        org.jsoup.nodes.Element p = new org.jsoup.nodes.Element("ul");
        p.appendText("t");
        org.jsoup.nodes.Element li1 = p.appendElement("li");
        p.appendText("x");
        org.jsoup.nodes.Element li2 = p.appendElement("li");
        Elements result = p.children();
        assertAll("Check mixed children filtering",
            () -> assertEquals(2, result.size(), "Expected two element children"),
            () -> assertSame(li1, result.get(0), "First element should match li1"),
            () -> assertSame(li2, result.get(1), "Second element should match li2")
        );
    }

    @Test
    @DisplayName("children() uses cachedChildren on second call when attributes.userData present")
    void test_TC05() {
        // First call builds cache via stashChildren; second call should hit cachedChildren branch
        org.jsoup.nodes.Element p = new org.jsoup.nodes.Element("div");
        org.jsoup.nodes.Element c = p.appendElement("p");
        // first invocation to populate cache
        Elements first = p.children();
        assertEquals(1, first.size(), "First call should have one child");
        // second invocation should use cached copy without rebuild
        Elements second = p.children();
        assertAll("Check cache hit returns same child",
            () -> assertEquals(1, second.size(), "Second call should still have one child"),
            () -> assertSame(c, second.get(0), "Cached child should be the same instance c")
        );
    }
}