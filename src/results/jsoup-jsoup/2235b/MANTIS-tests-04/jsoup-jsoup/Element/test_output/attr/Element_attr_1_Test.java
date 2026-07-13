package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Attributes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Element_attr_1_Test {

    @Test
    @DisplayName("attr allows empty attribute key and sets an attribute with empty name")
    public void test_TC05() {
        // GIVEN: an element with no attributes
        Element el = new Element("div");
        // WHEN: we set an attribute with empty key
        Element result = el.attr("", "empty");
        // THEN: method returns the same element (for chaining)
        assertSame(el, result, "attr should return the same Element instance for chaining");
        // AND: the attribute with empty name must be added with value "empty"
        Attributes attrs = el.attributes();
        assertEquals("empty", attrs.get(""), 
            "Empty key attribute should be present and have value 'empty'");
    }

    @Test
    @DisplayName("attr accepts empty string value and retains other attributes")
    public void test_TC06() {
        // GIVEN: an element with an existing attribute foo=bar
        Element el = new Element("span");
        el.attr("foo", "bar"); // ensures path B1→B4→B5 for foo
        // WHEN: we add a new attribute baz with empty value
        el.attr("baz", ""); // boundary: empty value
        // THEN: the original attribute remains unchanged
        Attributes attrs = el.attributes();
        assertEquals("bar", attrs.get("foo"), 
            "Existing attribute 'foo' should retain its original value 'bar'");
        // AND: the new attribute baz exists with empty string value
        assertEquals("", attrs.get("baz"), 
            "New attribute 'baz' should be added with an empty value");
    }
}