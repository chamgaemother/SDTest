package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Element_attr_0_Test {

    @Test
    @DisplayName("TC01: attr adds a new attribute when key and value are non-null and not previously present")
    void test_TC01() {
        // GIVEN an element with no attributes
        Element el = new Element("div");
        // WHEN adding a new attribute "data-test" -> this exercises branch where attribute list is initialized and contains no such key
        Element result = el.attr("data-test", "value");
        // THEN the return is the same element (for chaining) and the attribute is present with the correct value
        assertEquals(el, result, "attr should return the same element instance");
        assertEquals("value",
                     el.attributes().get("data-test"),
                     "The attribute 'data-test' should have been added with value 'value'");
    }

    @Test
    @DisplayName("TC02: attr overwrites an existing attribute value when the same key is used")
    void test_TC02() {
        // GIVEN an element with an existing attribute "id" -> this tests branch where key already exists
        Element el = new Element("span");
        el.attr("id", "first");
        assertEquals("first",
                     el.attributes().get("id"),
                     "Precondition: attribute 'id' should be 'first'");
        // WHEN overwriting the "id" attribute
        Element result = el.attr("id", "second");
        // THEN the return is the same element and the attribute value is updated
        assertEquals(el, result, "attr should return the same element instance");
        assertEquals("second",
                     el.attributes().get("id"),
                     "The existing attribute 'id' should have been overwritten to 'second'");
    }

    @Test
    @DisplayName("TC03: attr throws IllegalArgumentException when attribute key is null")
    void test_TC03() {
        // GIVEN an element
        Element el = new Element("p");
        // WHEN calling attr with null key -> exercises the Validate.notNull(key) branch throwing IllegalArgumentException
        IllegalArgumentException e = assertThrows(
            IllegalArgumentException.class,
            () -> el.attr(null, "v"),
            "attr should throw IllegalArgumentException when key is null"
        );
        // THEN no attribute is added
        assertTrue(el.attributes().isEmpty(),
                   "No attributes should have been added on exception");
    }

    @Test
    @DisplayName("TC04: attr throws IllegalArgumentException when attribute value is null")
    void test_TC04() {
        // GIVEN an element
        Element el = new Element("p");
        // WHEN calling attr with null value -> exercises the Validate.notNull(value) branch throwing IllegalArgumentException
        IllegalArgumentException e = assertThrows(
            IllegalArgumentException.class,
            () -> el.attr("key", null),
            "attr should throw IllegalArgumentException when value is null"
        );
        // THEN no attribute is added
        assertTrue(el.attributes().isEmpty(),
                   "No attributes should have been added on exception");
    }
}