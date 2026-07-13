package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Element.attr(String, String) method scenarios.
 */
public class Element_attr_1_Test {

    @Test
    @DisplayName("TC08: attr(String,String) with empty key throws IllegalArgumentException (key empty string)")
    public void test_TC08() {
        // GIVEN an Element and an empty attribute key to trigger validation failure (path B0→B1(keyEmpty)→B2)
        Element el = new Element("div");
        String emptyKey = "";
        String value = "value";
        // WHEN/THEN calling attr with an empty key should throw IllegalArgumentException
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> el.attr(emptyKey, value),
            "Expected attr(\"\", \"value\") to throw IllegalArgumentException for empty key"
        );
        // ensure no attribute was added after the exception
        assertFalse(el.attributes().hasKey(emptyKey),
            "No attribute should be added when key is empty"
        );
    }

    @Test
    @DisplayName("TC09: attr(String,String) with empty value sets attribute to empty string")
    public void test_TC09() {
        // GIVEN an Element and a valid key with an empty value (path B0→B3→B4(return))
        Element el = new Element("span");
        String key = "data-empty";
        String emptyValue = "";
        // WHEN calling attr with an empty value
        Element result = el.attr(key, emptyValue);
        // THEN the same element is returned for chaining
        assertSame(el, result,
            "attr should return the same element instance for chaining"
        );
        // and the attribute exists with an empty string value
        assertTrue(el.attributes().hasKey(key),
            "Attribute key should be present after setting empty value"
        );
        assertEquals("", el.attributes().get(key),
            "Attribute value should be empty string when provided empty value"
        );
    }
}