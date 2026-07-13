package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class Element_dataset_1_Test {

    @Test
    @DisplayName("dataset() converts hyphenated data- keys into camelCase map keys")
    void test_TC06() {
        // GIVEN an element with two data- attributes
        Element el = new Element("div");
        // attributes() was null so dataset() first call will initialize attributes (covers B1→B2)
        el.attributes().put("data-long-name", "v1");
        el.attributes().put("data-another-key", "v2");
        // WHEN retrieving the dataset (iterates over attribute entries, one per data- key, covers B3)
        Map<String, String> ds = el.dataset();
        // THEN the dataset map should have camelCase keys and correct values (covers B4→B5)
        assertEquals(2, ds.size(), "Should map two data- attributes");
        assertEquals("v1", ds.get("longName"), "Hyphenated 'data-long-name' should become 'longName'");
        assertEquals("v2", ds.get("anotherKey"), "Hyphenated 'data-another-key' should become 'anotherKey'");
    }

    @Test
    @DisplayName("dataset() returns a live view: modifying returned map updates underlying Attributes")
    void test_TC07() {
        // GIVEN an element with no preexisting attributes (attributes == null, covers B1 path)
        Element el = new Element("span");
        // WHEN obtaining dataset() (initializes attributes empty and returns view, covers B2→B4→B6)
        Map<String, String> ds = el.dataset();
        // Mutate the returned map: this should propagate to the element's attributes
        ds.put("newKey", "newVal");
        // THEN the underlying attribute 'data-new-key' should have been set to "newVal" (covers B7)
        assertEquals("newVal", el.attr("data-new-key"), 
            "Putting 'newKey' in dataset should create attribute 'data-new-key' with same value");
    }
}