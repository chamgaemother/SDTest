package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for org.jsoup.nodes.Element.attr overloads, covering both String and boolean variants.
 */
public class Element_attr_0_Test {

    @Test
    @DisplayName("TC01: attr(String,String) with new key adds attribute and returns same element")
    void test_TC01() {
        // GIVEN: an element with no existing attributes to test the add-new branch
        Element el = new Element("div");
        // WHEN: adding a new attribute
        Element ret = el.attr("foo", "bar");
        // THEN: the method returns same instance and attribute is set to expected value
        assertSame(el, ret, "Expected attr(...) to return the same Element instance");
        assertEquals("bar", el.attributes().get("foo"),
                "Expected newly added attribute 'foo' to have value 'bar'");
    }

    @Test
    @DisplayName("TC02: attr(String,String) with existing key overwrites value")
    void test_TC02() {
        // GIVEN: an element with an existing attribute 'key' to test the overwrite branch
        Element el = new Element("span");
        el.attr("key", "old");
        assertEquals("old", el.attributes().get("key"), "Sanity: precondition attribute 'key' should be 'old'");
        // WHEN: overwriting the existing attribute
        el.attr("key", "new");
        // THEN: the attribute value is updated
        assertEquals("new", el.attributes().get("key"),
                "Expected existing attribute 'key' to be overwritten with 'new'");
    }

    @Test
    @DisplayName("TC03: attr(String,String) with null key throws IllegalArgumentException")
    void test_TC03() {
        // GIVEN: an element and a null key to test exception on invalid input
        Element el = new Element("p");
        // WHEN/THEN: passing null key should throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> el.attr(null, "value"),
                "Expected attr(null, value) to throw IllegalArgumentException for null key");
    }

    @Test
    @DisplayName("TC04: attr(String,boolean) with true adds boolean attribute")
    void test_TC04() {
        // GIVEN: an element with no existing attributes to test boolean-true add branch
        Element el = new Element("a");
        // WHEN: setting boolean attribute to true adds an empty-value attribute
        Element ret = el.attr("hidden", true);
        // THEN: returns same element, key exists, and value is empty string
        assertSame(el, ret, "Expected attr(key, true) to return the same Element instance");
        assertTrue(el.attributes().hasKey("hidden"),
                "Expected boolean attribute 'hidden' to be present after setting true");
        assertEquals("", el.attributes().get("hidden"),
                "Expected boolean attribute 'hidden' to have empty string value");
    }

    @Test
    @DisplayName("TC05: attr(String,boolean) with false on non-existent key remains no attribute")
    void test_TC05() {
        // GIVEN: an element without the 'checked' attribute to test boolean-false no-op branch
        Element el = new Element("img");
        assertFalse(el.attributes().hasKey("checked"), "Sanity: 'checked' should not exist initially");
        // WHEN: setting boolean attribute to false on non-existent key
        Element ret = el.attr("checked", false);
        // THEN: attribute should remain absent
        assertSame(el, ret, "Expected attr(key, false) to return the same Element instance");
        assertFalse(el.attributes().hasKey("checked"),
                "Expected no 'checked' attribute after setting false on non-existent key");
    }

    @Test
    @DisplayName("TC06: attr(String,boolean) with false on existing key removes attribute")
    void test_TC06() {
        // GIVEN: an element with a boolean attribute 'flag' to test boolean-false removal branch
        Element el = new Element("li");
        el.attr("flag", true);
        assertTrue(el.attributes().hasKey("flag"), "Sanity: 'flag' should exist after setting true");
        // WHEN: setting boolean attribute to false removes the existing key
        Element ret = el.attr("flag", false);
        // THEN: attribute should be removed
        assertSame(el, ret, "Expected attr(key, false) to return the same Element instance");
        assertFalse(el.attributes().hasKey("flag"),
                "Expected 'flag' attribute to be removed after setting false on existing key");
    }

    @Test
    @DisplayName("TC07: attr(String,boolean) with null key throws NullPointerException")
    void test_TC07() {
        // GIVEN: an element and a null key to test exception on invalid boolean overload input
        Element el = new Element("ol");
        // WHEN/THEN: passing null key to boolean overload should throw NullPointerException
        assertThrows(NullPointerException.class, () -> el.attr(null, true),
                "Expected attr(null, true) to throw NullPointerException for null key");
    }
}