package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
public class Element_attr_2_Test {

    @Test
    @DisplayName("Calling attr on an element whose attributes field is already non-null (no new initialization) returns this and updates value")
    public void test_TC07() {
        // GIVEN an element with an already initialized attributes map (to exercise existing-attributes path B1→B2→B4)
        Element el = new Element("div");
        el.attributes(); // force attributes initialization so attributes != null
        el.attr("key", "first"); // initial set to ensure attr overrides existing entry

        // WHEN calling attr again on the same key to update
        Element returned = el.attr("key", "second");

        // THEN the returned instance should be the same element (chainable)
        assertSame(el, returned, "attr should return the same Element instance for chaining");
        // AND the attribute value should be updated to the new value
        assertEquals("second", el.attributes().get("key"), "attr should overwrite existing attribute value");
    }

    @Test
    @DisplayName("Chaining multiple attr calls with different keys preserves previously set entries and returns this")
    public void test_TC08() {
        // GIVEN a fresh element (attributes are initially null, testing initialization on first attr B0→B1→B2→B4)
        Element el = new Element("span");

        // WHEN chaining two attr calls with distinct keys
        el.attr("a", "1").attr("b", "2");
        // The first call initializes attributes and sets key "a"; the second call uses existing attributes to set "b"

        // THEN both attributes should be present with correct values
        assertEquals("1", el.attributes().get("a"), "First attr call should set key 'a' to '1'");
        assertEquals("2", el.attributes().get("b"), "Second attr call should set key 'b' to '2'");
    }
}