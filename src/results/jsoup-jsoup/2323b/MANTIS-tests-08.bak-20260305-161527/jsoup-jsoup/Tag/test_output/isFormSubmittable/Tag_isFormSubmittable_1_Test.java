package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.jsoup.parser.ParseSettings; // Added import for ParseSettings
public class Tag_isFormSubmittable_1_Test {

    @Test
    @DisplayName("Known submit tag with uppercase name and preserveCase true triggers clone path and returns true")
    public void test_TC04() {
        // Input CASE: uppercase "INPUT" with preserveCase=true so first lookup by tagName fails B1, then normalName lookup B2 succeeds, B3=true,
        // preserveCase so tagName("INPUT") != normalName and clone path B4 taken -> new instance
        String tagName = "INPUT";
        ParseSettings settings = ParseSettings.preserveCase; // No longer causes a compile error
        Tag tag = Tag.valueOf(tagName, settings); // Updated to use settings
        // ensure clone: new instance, not the static known tag for "input"
        assertNotSame(Tag.valueOf("input"), tag, "Expected a cloned instance, not the original static Tag");
        // should be form submittable since "input" is in formSubmitTags
        assertTrue(tag.isFormSubmittable(), "Cloned INPUT tag should be form submittable");
    }

    @Test
    @DisplayName("Known submit tag with non-default namespace falls through to default creation and returns false")
    public void test_TC05() {
        // Input CASE: known submit tag "input" but custom namespace so existing static tag has namespace HTML, mismatch at B3 -> default new Tag B5
        String tagName = "input";
        String customNs = "urn:custom";
        ParseSettings settings = ParseSettings.preserveCase; // No longer causes a compile error
        Tag tag = Tag.valueOf(tagName, customNs, settings); // Updated to use settings
        // should be a new instance with custom namespace, not the static one
        assertEquals(customNs, tag.namespace(), "Expected namespace to be set to custom namespace");
        // newly created default tag has isBlock=false, preserveWhitespace false, and formSubmit default false
        assertFalse(tag.isFormSubmittable(), "Newly created tag in custom namespace should not be form submittable");
    }

    @Test
    @DisplayName("Known submit tag with mixed-case name and preserveCase false returns original instance and true")
    public void test_TC06() {
        // Input CASE: mixed-case "InPuT" with preserveLowerCase=false (i.e. normalize to lower) so tagName trimmed to normalName
        // B1 false (no tagName match), B2 true(normalName match), B3 true, preserveCase=false so no clone at B4, return static
        String tagName = "InPuT";
        ParseSettings settings = ParseSettings.preserveLowerCase; // No longer causes a compile error
        Tag tag = Tag.valueOf(tagName, settings); // Updated to use settings
        // should return the original static tag for "input"
        Tag staticInput = Tag.valueOf("input");
        assertSame(staticInput, tag, "Expected the static Tag instance when preserveCase is false");
        // static input tag is form submittable
        assertTrue(tag.isFormSubmittable(), "Static INPUT tag should be form submittable");
    }
}