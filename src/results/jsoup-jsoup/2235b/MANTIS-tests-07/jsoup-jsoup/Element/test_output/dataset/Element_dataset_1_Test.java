package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
public class Element_dataset_1_Test {

    @Test
    @DisplayName("dataset() transforms dashed data- attribute names to camelCase keys")
    public void test_TC06() {
        // GIVEN: element with a data-pkg-name attribute (attributes != null, one entry to loop)
        Element el = new Element("div");
        el.attr("data-pkg-name", "jsoup-lib");
        // WHEN: retrieving the dataset view
        Map<String, String> ds = el.dataset();
        // THEN: size 1, camelCase key "pkgName" present and correctly mapped
        assertEquals(1, ds.size(), "Expected exactly one entry in dataset");
        assertTrue(ds.containsKey("pkgName"), "Expected camelCase key 'pkgName'");
        assertEquals("jsoup-lib", ds.get("pkgName"), "Expected value 'jsoup-lib' for key 'pkgName'");
    }

    @Test
    @DisplayName("Removing an entry from dataset() view removes the underlying data- attribute")
    public void test_TC07() {
        // GIVEN: element with data-key and a separate non-data attribute foo
        Element el = new Element("span");
        el.attr("data-key", "value");
        el.attr("foo", "bar");
        Map<String, String> ds = el.dataset(); // attributes != null; one data entry to iterate
        // WHEN: remove the dataset key
        ds.remove("key"); // should reflect removal in the underlying attributes
        // THEN: data-key attribute no longer exists, foo remains unchanged
        assertFalse(el.hasAttr("data-key"), "Expected 'data-key' attribute to be removed");
        assertEquals("bar", el.attr("foo"), "Expected non-data attribute 'foo' to remain unchanged");
    }

    @Test
    @DisplayName("Calling dataset().put(null, \"x\") throws NullPointerException")
    public void test_TC08() {
        // GIVEN: fresh element with no pre-existing attributes (attributes == null initially)
        Element el = new Element("div");
        Map<String, String> ds = el.dataset(); // attributes created internally; no data entries yet
        // WHEN/THEN: putting a null key should throw NPE
        assertThrows(NullPointerException.class, () -> ds.put(null, "x"),
                "Putting a null key into dataset view must throw NullPointerException");
    }
}