package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUnit 5 tests for Tag.isFormSubmittable method.
 */
public class Tag_isFormSubmittable_0_Test {

    @Test
    @DisplayName("Known form-submittable tag \"input\" returns true (formSubmit==true)")
    public void test_TC01() {
        // Using a predefined submit tag 'input' where formSubmit flag should be true
        Tag tag = Tag.valueOf("input");
        boolean result = tag.isFormSubmittable();
        assertEquals(true, result, "Expected isFormSubmittable() to return true for known submit tag 'input'.");
    }

    @Test
    @DisplayName("Known non-submit tag \"div\" returns false (formSubmit==false)")
    public void test_TC02() {
        // Using a predefined non-submit tag 'div' where formSubmit flag should be false
        Tag tag = Tag.valueOf("div");
        boolean result = tag.isFormSubmittable();
        assertEquals(false, result, "Expected isFormSubmittable() to return false for known non-submit tag 'div'.");
    }

    @Test
    @DisplayName("Unknown tag \"custom\" returns false (default formSubmit==false)")
    public void test_TC03() {
        // Using an unknown tag 'custom' created generically where formSubmit defaults to false
        Tag tag = Tag.valueOf("custom");
        boolean result = tag.isFormSubmittable();
        assertEquals(false, result, "Expected isFormSubmittable() to return false for unknown tag 'custom'.");
    }
}