package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Attributes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Element_attr_1_Test {

    @Test
    @DisplayName("attr(String,String) with empty value stores empty string without error")
    public void test_TC08() {
        // Given an element with no existing 'foo' attribute
        Element el = new Element("div");
        String key = "foo";
        String value = "";
        // When: setting an attribute with an empty string value
        Element ret = el.attr(key, value);
        // Then: should return the same element (chainable)
        assertSame(el, ret, "attr should return the same Element instance for chaining");
        // And: the attribute 'foo' should now exist with an empty string value
        Attributes attrs = el.attributes();
        assertTrue(attrs.hasKey(key), "attributes should contain the key 'foo' after setting empty value");
        assertEquals("", attrs.get(key), "the value for 'foo' should be the empty string");
    }

    @Test
    @DisplayName("attr(String,String) with null value throws IllegalArgumentException")
    public void test_TC09() {
        // Given an element with no attributes
        Element el = new Element("span");
        String key = "foo";
        String value = null;
        // When/Then: setting an attribute with a null value should fail fast
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> el.attr(key, value),
            "attr should throw IllegalArgumentException when value is null");
        // Optionally verify exception message mentions null value
        assertTrue(ex.getMessage().toLowerCase().contains("null"),
            "exception message should indicate null is not allowed");
    }
}