package org.jsoup.nodes;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Element_attr_2_Test {

    @Test
    @DisplayName("TC10: attr(String key, String value) sets a new attribute when value is empty string (covers B0→B1→B3→B4)")
    public void test_TC10() {
        // GIVEN an empty element and an empty string value, to exercise the branch where a new attribute is added with empty value
        Element el = new Element("div");
        String key = "empty";
        String val = "";
        // WHEN
        Element returned = el.attr(key, val);
        // THEN
        // returned should be the same instance as el
        assertSame(el, returned, "Method should return this element instance");
        // the attribute key should be present
        Attributes attrs = el.attributes();
        assertTrue(attrs.hasKey(key), "Attribute 'empty' must be present after setting with empty value");
        // and its value should be the empty string
        assertEquals("", attrs.get(key), "Attribute 'empty' should have empty string as its value");
    }

    @Test
    @DisplayName("TC11: attr(String key, String value) replaces existing attribute value with empty string (covers B0→B1→B3→B4)")
    public void test_TC11() {
        // GIVEN an element with an existing attribute 'foo'='bar', to exercise the branch where an existing attribute is updated to empty
        Element el = new Element("span");
        // precondition: set foo=bar
        el.attr("foo", "bar");
        // WHEN: replace with empty
        Element returned = el.attr("foo", "");
        // THEN
        // returned should still be the same instance
        assertSame(el, returned, "Method should return this element instance even when replacing");
        // attribute 'foo' must still exist
        Attributes attrs = el.attributes();
        assertTrue(attrs.hasKey("foo"), "Attribute 'foo' must still be present after replacement with empty value");
        // and its new value should be the empty string
        assertEquals("", attrs.get("foo"), "Attribute 'foo' should be updated to empty string");
    }
}