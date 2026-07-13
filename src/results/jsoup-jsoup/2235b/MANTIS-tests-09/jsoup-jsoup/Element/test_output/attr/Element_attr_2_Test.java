package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Element_attr_2_Test {

    @Test
    @DisplayName("TC08: attr(String,String) returns this Element instance for chaining when adding a new attribute")
    public void test_TC08() {
        // GIVEN an element with no attributes
        Element el = new Element("span");
        // WHEN we add a new attribute "data-test" -> this exercises the normal path B0→B1→B2→B3
        Element returned = el.attr("data-test", "xyz");
        // THEN we expect chaining: same instance returned
        assertSame(el, returned, "attr should return the same Element instance for chaining");
        // AND the attribute should be present with the correct value
        assertEquals("xyz", el.attributes().get("data-test"),
                     "The attribute 'data-test' should have been set to 'xyz'");
    }

    @Test
    @DisplayName("TC09: attr(String,String) adds attribute with empty-string value when value is empty")
    public void test_TC09() {
        // GIVEN an element with no attributes
        Element el = new Element("div");
        // WHEN we add an attribute with an empty string value -> still follows B0→B1→B2→B3
        el.attr("title", "");
        // THEN the attribute key must exist even if the value is empty
        assertTrue(el.attributes().hasKey("title"),
                   "The attribute 'title' should be present even when set to an empty string");
        // AND its stored value must be exactly the empty string
        assertEquals("", el.attributes().get("title"),
                     "The attribute 'title' value should be the empty string");
        // AND only one attribute should be in the map
        assertEquals(1, el.attributes().size(),
                     "Exactly one attribute should exist after setting an empty-string value");
    }
}