package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
public class Element_dataset_0_Test {

    @Test
    @DisplayName("dataset() on new Element with no attributes returns empty map (attributes==null branch, zero data- entries)")
    public void test_TC01() {
        // GIVEN a fresh element with null attributes (no data- entries)
        Element el = new Element("div");
        // WHEN
        Map<String, String> data = el.dataset();
        // THEN expect empty map when no data- attributes present
        assertTrue(data.isEmpty(), "Expected dataset to be empty when no attributes set");
    }

    @Test
    @DisplayName("dataset() returns single-entry map for one data- attribute (one iteration)")
    public void test_TC02() {
        // GIVEN an element with one data- prefixed attribute
        Element el = new Element("span");
        el.attr("data-key", "value"); // one data- entry -> loop runs once
        // WHEN
        Map<String, String> data = el.dataset();
        // THEN map size 1 and contains the correct key/value
        assertEquals(1, data.size(), "Expected one entry in dataset");
        assertEquals("value", data.get("key"), "Expected dataset to contain key 'key' with value 'value'");
    }

    @Test
    @DisplayName("dataset() filters only data- attributes among mixed attributes (multiple iterations)")
    public void test_TC03() {
        // GIVEN mixed attributes including two data- and one non-data (class)
        Element el = new Element("p");
        el.attr("data-one", "1");  // should be included
        el.attr("class", "cls");    // should be omitted
        el.attr("data-two", "2");  // should be included
        // WHEN
        Map<String, String> data = el.dataset();
        // THEN expect two entries for keys 'one' and 'two', class omitted
        assertEquals(2, data.size(), "Expected two data entries in dataset");
        assertTrue(data.containsKey("one"), "Expected key 'one' in dataset");
        assertEquals("1", data.get("one"));
        assertTrue(data.containsKey("two"), "Expected key 'two' in dataset");
        assertEquals("2", data.get("two"));
    }

    @Test
    @DisplayName("dataset() view is live: put into returned map adds attribute to element")
    public void test_TC04() {
        // GIVEN an element with no data- attributes
        Element el = new Element("div");
        Map<String, String> data = el.dataset(); // live view expected
        // WHEN putting a new entry into the view
        data.put("new", "v"); // should add attribute data-new on element
        // THEN element should reflect the new attribute
        assertEquals("v", el.attr("data-new"), "Expected element to have attribute 'data-new' with value 'v'");
    }

    @Test
    @DisplayName("dataset() view is live: remove from returned map deletes attribute on element")
    public void test_TC05() {
        // GIVEN an element with a data- attribute present
        Element el = new Element("div");
        el.attr("data-rem", "x"); // initial data-rem attribute
        Map<String, String> data = el.dataset(); // live view
        // WHEN removing the key 'rem' from the map
        data.remove("rem"); // should remove attribute data-rem on element
        // THEN element should no longer have that attribute
        assertFalse(el.hasAttr("data-rem"), "Expected element to no longer have attribute 'data-rem'");
    }
}