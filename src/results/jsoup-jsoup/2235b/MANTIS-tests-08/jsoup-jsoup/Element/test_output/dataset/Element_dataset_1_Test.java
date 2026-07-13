package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
public class Element_dataset_1_Test {

    @Test
    @DisplayName("TC11: dataset() handles attribute key with only 'data-' prefix (empty data key branch)")
    public void test_TC11() {
        // GIVEN an element with a 'data-' attribute only, triggering empty key branch
        Element el = new Element("div");
        el.attributes().put("data-", "empty");
        // WHEN retrieving dataset
        Map<String, String> ds = el.dataset();
        // THEN the map should contain exactly one entry with empty string key
        assertEquals(1, ds.size(), "Expected one dataset entry for 'data-' prefix only");
        assertTrue(ds.containsKey(""), "Dataset should contain empty string key for 'data-'");
        assertEquals("empty", ds.get(""), "Value for empty key should match attribute value");
    }

    @Test
    @DisplayName("TC12: dataset() converts hyphenated names to camelCase (data-user-name → userName branch)")
    public void test_TC12() {
        // GIVEN an element with a hyphenated data attribute, triggering camelCase conversion
        Element el = new Element("span");
        el.attr("data-user-name", "john");
        // WHEN retrieving dataset
        Map<String, String> ds = el.dataset();
        // THEN the key 'userName' should exist with the correct value
        assertEquals(1, ds.size(), "Dataset should have exactly one entry");
        assertTrue(ds.containsKey("userName"), "Hyphenated key should convert to camelCase 'userName'");
        assertEquals("john", ds.get("userName"), "Value should be preserved as 'john'");
    }

    @Test
    @DisplayName("TC13: Mutating dataset.clear() removes all underlying data- attributes (live view clear branch)")
    public void test_TC13() {
        // GIVEN an element with multiple data- attributes, ensuring live view reflect changes
        Element el = new Element("div");
        el.attr("data-a", "1");
        el.attr("data-b", "2");
        Map<String, String> ds = el.dataset();
        assertEquals(2, ds.size(), "Precondition: dataset should contain two entries");
        // WHEN clearing the dataset map
        ds.clear();
        // THEN the original data- attributes should be removed from the element
        assertFalse(el.hasAttr("data-a"), "After clear, 'data-a' attribute should be removed");
        assertFalse(el.hasAttr("data-b"), "After clear, 'data-b' attribute should be removed");
    }

    @Test
    @DisplayName("TC14: dataset().put(null, …) throws NullPointerException (null-key exception branch)")
    public void test_TC14() {
        // GIVEN an empty element dataset
        Element el = new Element("div");
        Map<String, String> ds = el.dataset();
        // WHEN attempting to put a null key, THEN a NullPointerException should be thrown
        assertThrows(NullPointerException.class, () -> ds.put(null, "v"),
            "Putting null key into dataset map should throw NullPointerException");
    }
}