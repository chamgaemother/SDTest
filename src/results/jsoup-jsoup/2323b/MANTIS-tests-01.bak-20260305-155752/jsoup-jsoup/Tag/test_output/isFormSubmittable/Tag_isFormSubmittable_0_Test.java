package org.jsoup.parser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.parser.ParseSettings; // Added missing import for ParseSettings
import org.jsoup.parser.Tag; // Added missing import for Tag

/**
 * JUnit tests for Tag.isFormSubmittable().
 */
public class Tag_isFormSubmittable_0_Test {

    @Test
    @DisplayName("Unknown tag created via valueOf(String,String,ParseSettings) has formSubmit=false")
    public void test_TC01() {
        // The tag "foo" is not in the predefined formSubmitTags list, so its formSubmit flag should remain false
        Tag t = Tag.valueOf("foo", "ns", ParseSettings.preserveCase);
        boolean result = t.isFormSubmittable();
        assertFalse(result, "Unknown tags should not be marked form-submittable");
    }

    @Test
    @DisplayName("Known form-submit tag \"input\" returns formSubmit=true")
    public void test_TC02() {
        // The tag "input" is in the predefined formSubmitTags list, so its formSubmit flag should be true
        Tag t = Tag.valueOf("input");
        boolean result = t.isFormSubmittable();
        assertTrue(result, "Known form-submittable tags like 'input' should return true");
    }
}