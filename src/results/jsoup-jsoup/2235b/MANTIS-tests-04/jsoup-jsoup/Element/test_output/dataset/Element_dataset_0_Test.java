package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
public class Element_dataset_0_Test {

    @Test
    @DisplayName("dataset returns empty map when no attributes present (attributes()==null branch false, loop-0)")
    void test_TC01() {
        // GIVEN an element with no attributes: attributes() will create Attributes, but no data- keys
        Element el = new Element("div");
        // WHEN
        Map<String, String> ds = el.dataset();
        // THEN dataset should be empty since no data- attributes to include (loop-0)
        assertTrue(ds.isEmpty(), "Expected empty dataset when no data- attributes are present");
    }

    @Test
    @DisplayName("dataset returns single entry for one data- attribute (loop-1, include data- prefix filtering)")
    void test_TC02() {
        // GIVEN an element with one data- attribute: only that entry should appear
        Element el = new Element("span");
        el.attr("data-foo", "bar");
        // WHEN
        Map<String, String> ds = el.dataset();
        // THEN map size 1 and key "foo" with value "bar"
        assertEquals(1, ds.size(), "Dataset should contain exactly one entry");
        assertEquals("bar", ds.get("foo"), "Dataset should return value for key 'foo'");
    }

    @Test
    @DisplayName("dataset filters out non-data- attributes and returns multiple data entries (loop-N)")
    void test_TC03() {
        // GIVEN an element with mixed attributes: only data- ones (data-a, data-b) should be returned
        Element el = new Element("p");
        el.attr("id", "x");
        el.attr("data-a", "1");
        el.attr("data-b", "2");
        el.attr("title", "t");
        // WHEN
        Map<String, String> ds = el.dataset();
        // THEN dataset size 2 and contains entries for "a","b" with correct values
        assertEquals(2, ds.size(), "Dataset should contain two data- entries");
        assertEquals("1", ds.get("a"), "Dataset should have value '1' for key 'a'");
        assertEquals("2", ds.get("b"), "Dataset should have value '2' for key 'b'");
        // non-data keys should not appear
        assertFalse(ds.containsKey("id"), "Dataset should not include non-data key 'id'");
        assertFalse(ds.containsKey("title"), "Dataset should not include non-data key 'title'");
    }

    @Test
    @DisplayName("dataset view is live: modifying returned map updates underlying attributes (loop-1, mutation)")
    void test_TC04() {
        // GIVEN an element with one data- attribute: dataset view should reflect mutations back to attributes
        Element el = new Element("div");
        el.attr("data-x", "y");
        // WHEN
        Map<String, String> ds = el.dataset();
        ds.put("z", "v"); // mutate dataset view
        // THEN the underlying element should have attribute data-z=="v" and dataset reflects it
        assertEquals("v", el.attr("data-z"), "Underlying attributes should be updated when dataset is mutated");
        assertEquals("v", ds.get("z"), "Dataset view should reflect newly added entry");
    }

    @Test
    @DisplayName("dataset respects data-attribute casing and trimmed keys (loop-1 mixed case)")
    void test_TC05() {
        // GIVEN an element with data-FooBar attribute: key casing should be preserved
        Element el = new Element("div");
        el.attr("data-FooBar", "baz");
        // WHEN
        Map<String, String> ds = el.dataset();
        // THEN dataset size 1, contains key "FooBar" with value "baz"
        assertEquals(1, ds.size(), "Dataset should contain exactly one entry for mixed-case key");
        assertTrue(ds.containsKey("FooBar"), "Dataset should preserve mixed-case key 'FooBar'");
        assertEquals("baz", ds.get("FooBar"), "Dataset should return correct value for key 'FooBar'");
    }
}