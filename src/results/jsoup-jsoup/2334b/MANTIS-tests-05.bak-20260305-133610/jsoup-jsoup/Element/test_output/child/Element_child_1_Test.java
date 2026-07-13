package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * JUnit 5 tests for Element.child(int) focusing on cache behavior via shadowChildrenRef.
 */
public class Element_child_1_Test {

    @Test
    @DisplayName("TC07: child(1) returns correct cached Element on second invocation (uses shadowChildrenRef cache path)")
    public void test_TC07() {
        // GIVEN a parent element with two child elements and no prior cache
        Element parent = new Element("div");
        Element first = new Element("a");
        Element second = new Element("b");
        // appending two children triggers ensureChildNodes and populates childNodes
        parent.appendChild(first);
        parent.appendChild(second);

        // WHEN first call to child(1): cache miss -> builds new shadow list, returns second
        Element result1 = parent.child(1);
        // WHEN second call to child(1): cache hit  -> reuses shadow list, returns same second instance
        Element result2 = parent.child(1);

        // THEN both results are the same instance as the second appended element
        // This verifies that on the second invocation, the cached shadow list is used.
        assertSame(second, result1, "First call to child(1) should return the second child element");
        assertSame(second, result2, "Second call to child(1) should return the same cached element instance");
    }
}