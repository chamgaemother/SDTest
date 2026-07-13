package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for org.jsoup.nodes.Element.attr(String, String)
 */
public class Element_attr_0_Test {

    @Test
    @DisplayName("TC01: Setting a new attribute when attributes() is initially null")
    public void test_TC01() {
        // GIVEN an Element with no initial attributes (attributes field null)
        Element el = new Element("div");
        // WHEN calling attr on a fresh element, which should initialize attributes (branch B1->B2->B3)
        Element returned = el.attr("foo", "bar");
        // THEN the returned Element is the same instance for chaining
        assertSame(el, returned);
        // AND attributes() should now be non-null and contain the new attribute
        assertNotNull(el.attributes(), "attributes() should be initialized when first attr is set");
        assertEquals("bar", el.attributes().get("foo"), "Expected attribute 'foo' to have value 'bar'");
    }

    @Test
    @DisplayName("TC02: Updating an existing attribute value when attributes() is already non-null")
    public void test_TC02() {
        // GIVEN an Element that already has attribute foo=old (attributes initialized)
        Element el = new Element("div");
        el.attr("foo", "old"); // first set creates attributes
        // WHEN updating the same attribute key, taking branch where attributes() is non-null
        el.attr("foo", "new");
        // THEN the attribute value should be updated to 'new'
        assertEquals("new", el.attributes().get("foo"), "Expected attribute 'foo' to be updated to 'new'");
    }

    @Test
    @DisplayName("TC03: Passing null key throws IllegalArgumentException")
    public void test_TC03() {
        // GIVEN an Element span
        Element el = new Element("span");
        // WHEN calling attr with null key, should trigger Validate.notNull(key) and throw IllegalArgumentException
        IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> el.attr(null, "value"),
            "Expected IllegalArgumentException when key is null"
        );
        // No attribute should be added when exception is thrown
        assertTrue(el.attributes() == null || !el.attributes().hasKey("value"),
                   "No attributes should be added on exception");
    }

    @Test
    @DisplayName("TC04: Passing null value throws IllegalArgumentException")
    public void test_TC04() {
        // GIVEN an Element span
        Element el = new Element("span");
        // WHEN calling attr with null value, should trigger Validate.notNull(value) and throw IllegalArgumentException
        IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> el.attr("key", null),
            "Expected IllegalArgumentException when value is null"
        );
        // No attribute should be added when exception is thrown
        assertTrue(el.attributes() == null || !el.attributes().hasKey("key"),
                   "No attributes should be added on exception");
    }
}