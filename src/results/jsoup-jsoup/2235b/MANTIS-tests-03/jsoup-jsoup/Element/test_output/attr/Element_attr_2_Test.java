package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Element_attr_2_Test {

    @Test
    @DisplayName("attr(String,String) returns this for call chaining")
    void test_TC09() {
        // GIVEN an element with no prior attributes
        Element el = new Element("div");
        // WHEN calling the String->String overload of attr (should set and return this)
        Element result = el.attr("key", "value");
        // THEN the returned object must be the same instance for chaining
        assertSame(el, result, "attr(key,value) should return the same Element instance for chaining");
        // And the attribute should have been added
        assertTrue(el.hasAttr("key"), "Attribute 'key' should be present after attr(key,value)");
        assertEquals("value", el.attr("key"), "Attribute 'key' value should be 'value'");
    }

    @Test
    @DisplayName("attr(String,boolean) true overload returns this for call chaining")
    void test_TC10() {
        // GIVEN an element with no prior attributes
        Element el = new Element("span");
        // WHEN calling the boolean overload with true (should set a boolean attribute and return this)
        Element result = el.attr("hidden", true);
        // THEN must return the same instance for chaining
        assertSame(el, result, "attr(hidden,true) should return the same Element instance for chaining");
        // And the boolean attribute should be present (value is empty string per boolean attr semantics)
        assertTrue(el.hasAttr("hidden"), "Boolean attribute 'hidden' should be present after attr(hidden,true)");
        assertEquals("", el.attr("hidden"), "Boolean attribute 'hidden' should have empty value");
    }

    @Test
    @DisplayName("attr(String,boolean) false overload returns this and removes attribute if absent")
    void test_TC11() {
        // GIVEN an element with no prior attributes, so attribute 'notPresent' is absent
        Element el = new Element("p");
        assertFalse(el.hasAttr("notPresent"), "Precondition: 'notPresent' should initially be absent");
        // WHEN calling the boolean overload with false (should be a no-op and return this)
        Element result = el.attr("notPresent", false);
        // THEN returned object must be same instance for chaining
        assertSame(el, result, "attr(notPresent,false) should return the same Element instance for chaining");
        // And attribute remains absent (false should remove it if present, no exception if absent)
        assertFalse(el.hasAttr("notPresent"), "Attribute 'notPresent' should remain absent after attr(notPresent,false)");
    }
}