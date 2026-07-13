package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for {@link Element#attr(String, String)} based on provided scenarios.
 */
public class Element_attr_1_Test {

    @Test
    @DisplayName("TC07: attr(String key, String value) throws IllegalArgumentException when key is empty string")
    public void test_TC07() {
        // GIVEN an Element and an empty key to trigger validation path for empty key (B0->B1->B2)
        Element el = new Element("div");
        String key = "";
        String value = "val";
        // WHEN/THEN: calling attr with empty key must throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> el.attr(key, value));
    }

    @Test
    @DisplayName("TC08: attr(String key, String value) throws IllegalArgumentException when value is null")
    public void test_TC08() {
        // GIVEN an Element and a null value to trigger validation path for null value (B0->B1->B2)
        Element el = new Element("div");
        String key = "foo";
        String value = null;
        // WHEN/THEN: calling attr with null value must throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> el.attr(key, value));
    }

    @Test
    @DisplayName("TC09: attr(String key, String value) returns this instance for chaining")
    public void test_TC09() {
        // GIVEN an Element and valid key/value to follow fluent path (B0->B1->B3->B4)
        Element el = new Element("span");
        // WHEN: calling attr with non-empty key and non-null value
        Element returned = el.attr("data-test", "123");
        // THEN: should return the same instance for method chaining
        assertSame(el, returned, "attr should return this instance for chaining");
    }
}