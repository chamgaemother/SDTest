package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
public class Element_dataset_2_Test {

    @Test
    @DisplayName("TC11: dataset() converts dashed data-attribute names to camelCase keys (hyphen-to-camel-case branch)")
    public void test_TC11() {
        // GIVEN an element with a single data-user-name attribute (tests loop×1 and hyphen-to-camel-case path)
        Element el = new Element("div");
        el.attr("data-user-name", "alice");
        // WHEN fetching the dataset map
        Map<String, String> ds = el.dataset();
        // THEN size is 1, key 'userName' exists and maps to original value
        assertAll("camelCase conversion and correct mapping",
            () -> assertEquals(1, ds.size(), "Expected exactly one entry"),
            () -> assertTrue(ds.containsKey("userName"), "Expected camelCased key 'userName'"),
            () -> assertEquals("alice", ds.get("userName"), "Expected the value to be 'alice'")
        );
    }

    @Test
    @DisplayName("TC12: dataset().put(key, null) removes the data- attribute and returns the previous value (null-value branch)")
    public void test_TC12() {
        // GIVEN an element with attribute data-x=orig (tests loop×1 and putExisting path)
        Element el = new Element("span");
        el.attr("data-x", "orig");
        Map<String, String> ds = el.dataset();
        // WHEN putting null for key 'x' → should remove the attribute
        String prev = ds.put("x", null);
        // THEN previous value returned, attribute removed, and dataset no longer contains 'x'
        assertAll("put-null removes attribute and returns previous value",
            () -> assertEquals("orig", prev, "Expected returned previous value 'orig'"),
            () -> assertFalse(el.hasAttr("data-x"), "Expected the underlying data-x attribute to be removed"),
            () -> assertFalse(ds.containsKey("x"), "Expected the dataset view not to contain 'x'")
        );
    }

    @Test
    @DisplayName("TC13: dataset().remove(nonexistent) returns null and makes no modifications (remove-nonexistent branch)")
    public void test_TC13() {
        // GIVEN a fresh element with no data- attributes (tests loop×0 and removeNonexistent path)
        Element el = new Element("div");
        Map<String, String> ds = el.dataset();
        // WHEN removing a non-existent key
        String result = ds.remove("noKey");
        // THEN result is null and dataset remains empty
        assertAll("remove non-existent key should be no-op",
            () -> assertNull(result, "Expected remove() to return null for non-existent key"),
            () -> assertTrue(ds.isEmpty(), "Expected the dataset to remain empty")
        );
    }

    @Test
    @DisplayName("TC14: dataset().remove(null) throws NullPointerException (remove-null-key exception path)")
    public void test_TC14() {
        // GIVEN a fresh element and its dataset (tests loop×0 and removeNull path)
        Element el = new Element("p");
        Map<String, String> ds = el.dataset();
        // WHEN removing null key → should throw NPE
        assertThrows(NullPointerException.class, () -> {
            ds.remove(null);
        }, "Expected NullPointerException when removing null key");
    }

    @Test
    @DisplayName("TC15: dataset().clear() on empty dataset does nothing (clear-zero loop path)")
    public void test_TC15() {
        // GIVEN an element with no data- attributes (tests loop×0 and clear path)
        Element el = new Element("div");
        Map<String, String> ds1 = el.dataset();
        // WHEN clearing the empty dataset
        ds1.clear();
        // THEN dataset remains empty and no errors occur
        assertAll("clear on empty dataset should be no-op",
            () -> assertTrue(ds1.isEmpty(), "Expected dataset to remain empty after clear()"),
            () -> assertTrue(el.dataset().isEmpty(), "Expected new dataset view to also be empty")
        );
    }
}