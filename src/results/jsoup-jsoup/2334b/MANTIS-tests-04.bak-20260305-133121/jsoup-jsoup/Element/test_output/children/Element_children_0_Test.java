package org.jsoup.nodes;

import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Test class for the children() method of Element class
public class Element_children_0_Test {

    @Test
    @DisplayName("children() returns empty Elements when no child nodes exist (childNodeSize==0)")
    public void test_TC01() {
        // Setup: new element with no children triggers the branch for empty child elements (childNodeSize == 0)
        Element parent = new Element("div");

        // Invoke children()
        Elements result = parent.children();

        // Expect an empty list since there are no child elements
        assertTrue(result.isEmpty(), "Expected no child elements for a newly created element without children");
    }

    @Test
    @DisplayName("children() returns one child and caches shadowChildrenRef on first call (childNodeSize==1, cache miss)")
    public void test_TC02() {
        // Setup: one child element appended, triggers path for single-child cache miss
        Element parent = new Element("div");
        Element child = parent.appendElement("span");

        // Invoke children() first time
        Elements result = parent.children();

        // Expect exactly one child, and that it is the same instance as appended
        assertAll(
            () -> assertEquals(1, result.size(), "Expected exactly one child element"),
            () -> assertSame(child, result.get(0), "Expected the returned child to be the appended element instance")
        );
    }

    @Test
    @DisplayName("children() returns multiple children and populates cache on first invocation (childNodeSize>1, cache miss, loop N)")
    public void test_TC03() {
        // Setup: two child elements appended, triggers path for multiple-child cache miss and iteration over both
        Element parent = new Element("ul");
        Element c1 = parent.appendElement("li");
        Element c2 = parent.appendElement("li");

        // Invoke children()
        Elements result = parent.children();

        // Expect two children in order of appending
        assertAll(
            () -> assertEquals(2, result.size(), "Expected two child elements"),
            () -> assertSame(c1, result.get(0), "First element should be the first appended li"),
            () -> assertSame(c2, result.get(1), "Second element should be the second appended li")
        );
    }

    @Test
    @DisplayName("children() returns cached children list on second call without mutation (cache hit, no rebuild)")
    public void test_TC04() {
        // Setup: one child, first call builds and caches the children list, second call should hit the cache
        Element parent = new Element("div");
        Element child = parent.appendElement("p");

        // First invocation builds cache
        Elements first = parent.children();
        // Second invocation should return the exact same Elements instance per intended cache-hit behavior
        Elements second = parent.children();

        // Verify identity and content
        assertAll(
            () -> assertSame(first, second, "Expected the same Elements instance on cache hit without mutation"),
            () -> assertEquals(1, second.size(), "Expected cached list to still contain one child element")
        );
    }

    @Test
    @DisplayName("children() rebuilds cache after mutation via appendChild (cache invalidation)")
    public void test_TC05() {
        // Setup: one child appended, build cache, then append another child to trigger cache invalidation
        Element parent = new Element("div");
        Element c1 = parent.appendElement("em");
        Elements first = parent.children();

        // Mutation: append a new child invalidates the previous cache
        Element c2 = parent.appendElement("strong");
        Elements second = parent.children();

        // Expect a new Elements instance with both children in order, and not equal to the old cached instance
        assertAll(
            () -> assertEquals(2, second.size(), "Expected two child elements after mutation"),
            () -> assertSame(c1, second.get(0), "First element after rebuild should remain the first appended element"),
            () -> assertSame(c2, second.get(1), "Second element after rebuild should be the newly appended element"),
            () -> assertNotSame(first, second, "Expected a new Elements instance after cache invalidation")
        );
    }
}