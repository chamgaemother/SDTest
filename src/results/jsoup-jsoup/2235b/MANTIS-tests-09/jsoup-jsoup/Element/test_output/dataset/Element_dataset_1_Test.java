package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
public class Element_dataset_1_Test {

    @Test
    @DisplayName("dataset() when attributes exist but no data- entries returns empty map (attributes != null, zero data entries)")
    public void test_TC06() {
        // GIVEN an element with a non-data attribute
        Element el = new Element("div");
        el.attr("other", "v"); // attribute key does not start with "data-"
        // WHEN dataset view is obtained
        Map<String, String> data = el.dataset();
        // THEN no data- entries are present, so view is empty
        assertTrue(data.isEmpty(), "Expected dataset view to be empty when only non-data attributes exist");
    }

    @Test
    @DisplayName("dataset() view clear() deletes all data- attributes from element (live view clear branch)")
    public void test_TC07() {
        // GIVEN an element with two data- attributes
        Element el = new Element("span");
        el.attr("data-a", "1");
        el.attr("data-b", "2");
        // WHEN the live dataset view is cleared
        Map<String, String> data = el.dataset();
        data.clear(); // should remove all data- entries from the element's attributes
        // THEN both data-a and data-b attributes are removed
        assertFalse(el.hasAttr("data-a"), "Expected data-a attribute to be removed after clear()");
        assertFalse(el.hasAttr("data-b"), "Expected data-b attribute to be removed after clear()");
    }

    @Test
    @DisplayName("dataset() view putAll() adds multiple data- attributes at once (live view putAll branch)")
    public void test_TC08() {
        // GIVEN an element with no initial data- attributes
        Element el = new Element("p");
        Map<String, String> additions = new HashMap<>();
        additions.put("x", "1");
        additions.put("y", "2");
        // WHEN we putAll on the dataset view
        Map<String, String> data = el.dataset();
        data.putAll(additions); // should map to data-x and data-y attributes
        // THEN the element has attributes data-x and data-y with correct values
        assertEquals("1", el.attr("data-x"), "Expected element to have data-x=1 after putAll");
        assertEquals("2", el.attr("data-y"), "Expected element to have data-y=2 after putAll");
    }

    @Test
    @DisplayName("dataset() includes attribute with empty key when attribute name is exactly 'data-' (boundary data- prefix only)")
    public void test_TC09() {
        // GIVEN an element with an attribute named exactly "data-" (key part is empty)
        Element el = new Element("div");
        el.attr("data-", "val"); // boundary case: prefix only, empty key
        // WHEN dataset view is obtained
        Map<String, String> data = el.dataset();
        // THEN the view contains one entry with empty key and value "val"
        assertEquals(1, data.size(), "Expected exactly one entry for 'data-' attribute");
        assertTrue(data.containsKey(""), "Expected key to be empty string for 'data-' attribute");
        assertEquals("val", data.get(""), "Expected value 'val' for empty key entry");
    }
}