package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Element.attr(String, String)
 */
public class Element_attr_1_Test {

    @Test
    @DisplayName("attr(String key, String value) throws IllegalArgumentException when value is null")
    public void test_TC06() {
        // Scenario TC06: key is non-null ("data"), value is null -> expect IllegalArgumentException
        Element el = new Element("div");  // precondition
        String key = "data";
        String value = null;
        // B0→B1(key non-null)→B2(value null)→B6: should throw on null value
        assertThrows(IllegalArgumentException.class, () -> el.attr(key, value));
    }

    @Test
    @DisplayName("attr(String key, String value) accepts empty-string key and adds attribute of empty name")
    public void test_TC07() {
        // Scenario TC07: key is empty string, value is non-null -> new attribute added
        Element el = new Element("p");  // precondition
        String key = "";
        String value = "v";
        // B0→B1(key non-null)→B2(value non-null)→B3(new attribute)
        el.attr(key, value);
        // Then attribute with empty name should exist and have the given value
        assertTrue(el.attributes().hasKey(""), "Expected an attribute with empty key to be present");
        assertEquals("v", el.attributes().get(""), "Attribute value for empty key should be 'v'");
    }

    @Test
    @DisplayName("attr(String key, String value) overrides existing attribute value when value is empty string")
    public void test_TC08() {
        // Scenario TC08: existing attribute 'title' present, override it with empty string
        Element el = new Element("span");  // precondition
        el.attr("title", "old");
        assertEquals("old", el.attributes().get("title"), "Precondition failed: initial 'title' should be 'old'");
        String key = "title";
        String value = ""; // empty string triggers override path
        // B0→B1(key non-null)→B2(value non-null empty)→B3(existing attribute)
        el.attr(key, value);
        // Then the 'title' attribute value should be updated to empty string
        assertEquals("", el.attributes().get("title"), "Expected 'title' attribute to be overridden with empty string");
    }
}