package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
public class Element_dataset_0_Test {

    @Test
    @DisplayName("dataset() on new Element with no attributes returns an empty map")
    public void test_TC01() {
        // Scenario TC01: attributes is null branch, no data-* entries
        Element el = new Element("div"); // attributes == null initially
        Map<String, String> ds = el.dataset();
        assertTrue(ds.isEmpty(), "Expected dataset to be empty when no attributes present");
    }

    @Test
    @DisplayName("dataset() on Element with non-data attributes returns an empty map")
    public void test_TC02() {
        // Scenario TC02: attributes != null but no keys start with 'data-'
        Element el = new Element("span");
        el.attr("class", "header"); // non data- attribute only
        Map<String, String> ds = el.dataset();
        assertTrue(ds.isEmpty(), "Expected dataset to be empty when only non-data attributes present");
    }

    @Test
    @DisplayName("dataset() on Element with one data-* attribute returns a singleton map")
    public void test_TC03() {
        // Scenario TC03: one data-* entry should be present
        Element el = new Element("p");
        el.attr("data-foo", "bar"); // single data- attribute
        Map<String, String> ds = el.dataset();
        assertEquals(1, ds.size(), "Expected exactly one entry in dataset");
        assertEquals("bar", ds.get("foo"), "Expected dataset to contain key 'foo' with value 'bar'");
    }

    @Test
    @DisplayName("dataset() on Element with mixed attributes returns only data-* entries in insertion order")
    public void test_TC04() {
        // Scenario TC04: mixed attrs with two data- keys and one non-data
        Element el = new Element("div");
        el.attr("data-a", "1");
        el.attr("id", "X"); // should be filtered out
        el.attr("data-b", "2");
        Map<String, String> ds = el.dataset();
        assertEquals(2, ds.size(), "Expected two data entries in dataset");
        // Verify insertion order of keys: a then b
        List<String> keys = new ArrayList<>(ds.keySet());
        assertEquals(Arrays.asList("a", "b"), keys, "Expected data keys in insertion order ['a','b']");
    }

    @Test
    @DisplayName("dataset() returns a live view: updates to returned map reflect in Element.attributes")
    public void test_TC05() {
        // Scenario TC05: dataset returns live view; modifications should update attributes
        Element el = new Element("div");
        Map<String, String> ds = el.dataset();
        ds.put("new", "val"); // modifying the returned map should add data-new attribute
        String attrVal = el.attr("data-new");
        assertEquals("val", attrVal, "Expected element to have attribute 'data-new' with value 'val' after dataset modification");
    }
}