package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for Element.child(int) method, covering cache hit and cache invalidation scenarios.
 */
public class Element_child_2_Test {

    @Test
    @DisplayName("child(1) returns the second element on repeated calls without modifying children (cache hit branch)")
    public void test_TC09() {
        // GIVEN an Element with two child elements appended, priming the cache
        Element parent = new Element("div");
        parent.appendElement("span"); // first child
        Element second = parent.appendElement("em"); // second child, to be returned by child(1)
        
        // WHEN retrieving the second element twice, expecting cache hit on second call
        Element firstCall = parent.child(1); // builds and caches shadow children list
        Element secondCall = parent.child(1); // should use cached list without rebuild
        
        // THEN both calls return the same instance as originally appended
        // Ensures the shadowChildrenRef cache path is taken B0->B1->B3->B4 then on second: B1->B2 (cache hit)->B4
        assertSame(second, firstCall, "First call to child(1) should return the appended second element");
        assertSame(second, secondCall, "Second call to child(1) should return the same instance from cache");
    }

    @Test
    @DisplayName("child(0) rebuilds and returns the new first element after a list modification (cache invalidation branch)")
    public void test_TC10() {
        // GIVEN an Element with one child, priming the cache for index 0
        Element parent = new Element("ul");
        Element first = parent.appendElement("li"); // only child
        Element initial = parent.child(0); // builds and caches shadow list
        
        // WHEN appending another child to invalidate the cache (ChangeNotifyingArrayList triggers nodelistChanged)
        parent.appendElement("li"); // modifies childNodes, should reset shadowChildrenRef
        Element afterMod = parent.child(0); // rebuilds shadow list and returns first element again
        
        // THEN initial and afterMod both refer to the original first element, verifying rebuild correctness
        // Path: initial: B0->B1->B3->B4; modification: B6 invalidates cache; afterMod: B1->B3(rebuild)->B4
        assertSame(first, initial, "Initial child(0) should return the first appended element");
        assertSame(first, afterMod, "After modification, child(0) should rebuild cache and still return the original first element");
    }
}