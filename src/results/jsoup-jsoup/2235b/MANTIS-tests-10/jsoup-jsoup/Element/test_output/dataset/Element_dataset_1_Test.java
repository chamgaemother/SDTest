package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Element.dataset() method behavior.
 */
public class Element_dataset_1_Test {

    @Test
    @DisplayName("TC06: Removing a data key via dataset().remove(...) removes the corresponding data-* attribute")
    public void test_TC06() {
        // GIVEN: Element with two data attributes data-foo and data-baz
        Element el = new Element("div");
        el.attr("data-foo", "bar");
        el.attr("data-baz", "qux");
        // WHEN: obtaining live dataset view and removing key 'foo'
        Map<String, String> ds = el.dataset(); // enters path B0→B1→B3
        ds.remove("foo");             // triggers removal path B5
        // THEN: 'foo' is removed, 'baz' remains
        assertFalse(el.hasAttr("data-foo"), "Expected data-foo attribute to be removed");
        assertEquals(1, ds.size(), "Expected only one entry in dataset after removal");
        assertEquals("qux", ds.get("baz"), "Expected remaining key 'baz' to map to 'qux'");
    }

    @Test
    @DisplayName("TC07: Clearing the dataset map via dataset().clear() removes all data-* attributes")
    public void test_TC07() {
        // GIVEN: Element with data-a, data-b, and a non-data id attribute
        Element el = new Element("span");
        el.attr("data-a", "1");
        el.attr("data-b", "2");
        el.attr("id", "X");
        // WHEN: obtaining live dataset view and clearing it
        Map<String, String> ds = el.dataset(); // enters path B0→B1→B3
        ds.clear();                         // triggers clear path B6
        // THEN: data attributes gone, id remains
        assertTrue(ds.isEmpty(), "Expected dataset to be empty after clear");
        assertFalse(el.hasAttr("data-a"), "Expected data-a attribute to be removed");
        assertFalse(el.hasAttr("data-b"), "Expected data-b attribute to be removed");
        assertTrue(el.hasAttr("id"), "Expected non-data 'id' attribute to remain unaffected");
    }

    @Test
    @DisplayName("TC08: dataset() normalizes uppercase data-* keys to lower camel-case keys")
    public void test_TC08() {
        // GIVEN: Element with attribute data-FooBar
        Element el = new Element("p");
        el.attr("data-FooBar", "baz");
        // WHEN: obtaining dataset, expecting key normalization (upper segments to camel case)
        Map<String, String> ds = el.dataset(); // enters path B0→B1→B2
        // THEN: key is normalized to 'fooBar'
        assertEquals(1, ds.size(), "Expected one normalized key in dataset");
        assertTrue(ds.containsKey("fooBar"), "Expected dataset to contain normalized key 'fooBar'");
        assertEquals("baz", ds.get("fooBar"), "Expected value for 'fooBar' to be 'baz'");
    }
}