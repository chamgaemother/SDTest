package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Element.attr(String, boolean) method based on provided scenarios.
 */
public class Element_attr_2_Test {

    @Test
    @DisplayName("attr(String,boolean) with false on a non-existing key does not add attribute and returns same element")
    public void test_TC10() {
        // GIVEN: an element with no 'foo' attribute
        Element el = new Element("div");
        assertFalse(el.attributes().hasKey("foo"), "Precondition failed: 'foo' should not exist initially");

        // WHEN: calling attr with false should not add the attribute (boolean false is a no-op)
        Element result = el.attr("foo", false);

        // THEN: the same element instance is returned and 'foo' is still not present
        assertSame(el, result, "Expected the same Element instance to be returned");
        assertFalse(el.attributes().hasKey("foo"), "Attribute 'foo' should not be added when setting boolean false");
    }

    @Test
    @DisplayName("attr(String,boolean) with empty-string key adds boolean attribute with empty value")
    public void test_TC11() {
        // GIVEN: an element with no '' (empty string) attribute
        Element el = new Element("span");
        assertFalse(el.attributes().hasKey(""), "Precondition failed: empty key should not exist initially");

        // WHEN: calling attr with empty key and true should add an attribute with empty name and empty value
        Element result = el.attr("", true);

        // THEN: the same element instance is returned and empty key exists with empty value
        assertSame(el, result, "Expected the same Element instance to be returned");
        assertTrue(el.attributes().hasKey(""), "Attribute with empty key should be present after setting boolean true");
        assertEquals("", el.attributes().get(""), "The value for the empty key attribute should be an empty string");
    }
}