package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Attributes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Element_attr_0_Test {

    @Test
    @DisplayName("TC01: attr(String,String): new attribute key not present adds attribute (branch: key not in attributes)")
    public void test_TC01() {
        // GIVEN an Element with no attributes
        Element el = new Element("div");
        // WHEN setting a new attribute "data-test" which is not present
        el.attr("data-test", "123");
        // THEN the new attribute must be added with the correct value and size==1
        Attributes attrs = el.attributes();
        assertEquals("123", attrs.get("data-test"), 
            "Expected the new attribute 'data-test' to be present with value '123'");
        assertEquals(1, attrs.size(), 
            "Expected exactly one attribute after adding a new one");
    }

    @Test
    @DisplayName("TC02: attr(String,String): existing attribute key updates value (branch: key present in attributes)")
    public void test_TC02() {
        // GIVEN an Element with an existing attribute "foo"="bar"
        Element el = new Element("div");
        el.attr("foo", "bar"); // initial add
        // WHEN updating the existing attribute "foo" to "baz"
        el.attr("foo", "baz");
        // THEN the attribute value is updated and no duplicate key is created
        Attributes attrs = el.attributes();
        assertEquals("baz", attrs.get("foo"), 
            "Expected the existing attribute 'foo' to be updated to 'baz'");
        assertEquals(1, attrs.size(), 
            "Expected attribute count to remain 1 after updating existing key");
    }

    @Test
    @DisplayName("TC03: attr(String,String): null key throws IllegalArgumentException (branch: Validate.notNull(key) fails)")
    public void test_TC03() {
        // GIVEN a null key
        Element el = new Element("div");
        // WHEN calling attr with null key, THEN IllegalArgumentException must be thrown
        assertThrows(IllegalArgumentException.class, () -> el.attr(null, "val"), 
            "Expected IllegalArgumentException when key is null");
    }

    @Test
    @DisplayName("TC04: attr(String,boolean): true adds boolean attribute with empty value (branch: attributeValue true)")
    public void test_TC04() {
        // GIVEN an Element with no attributes
        Element el = new Element("button");
        // WHEN calling attr with boolean true
        el.attr("disabled", true);
        // THEN the attribute "disabled" must exist with empty string value
        Attributes attrs = el.attributes();
        assertTrue(attrs.hasKey("disabled"), 
            "Expected boolean attribute 'disabled' to be present when set to true");
        assertEquals("", attrs.get("disabled"), 
            "Expected the value of boolean attribute 'disabled' to be an empty string");
    }

    @Test
    @DisplayName("TC05: attr(String,boolean): false removes existing boolean attribute (branch: attributeValue false)")
    public void test_TC05() {
        // GIVEN an Element with a boolean attribute "hidden" set to true
        Element el = new Element("div");
        el.attr("hidden", true); // ensure it's present
        assertTrue(el.attributes().hasKey("hidden"), 
            "Sanity check: 'hidden' should exist after setting to true");
        // WHEN calling attr with boolean false on the same key
        el.attr("hidden", false);
        // THEN the attribute "hidden" should be removed
        assertFalse(el.attributes().hasKey("hidden"), 
            "Expected boolean attribute 'hidden' to be removed when set to false");
    }
}