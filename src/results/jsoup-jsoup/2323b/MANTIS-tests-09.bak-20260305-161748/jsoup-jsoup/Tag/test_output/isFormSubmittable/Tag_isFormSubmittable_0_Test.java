package org.jsoup.parser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test class for org.jsoup.parser.Tag.isFormSubmittable()
 */
public class Tag_isFormSubmittable_0_Test {

    @Test
    @DisplayName("isFormSubmittable returns true for a predefined submittable form control tag (input)")
    public void test_TC01() {
        // Given: a known HTML input tag, which should be submittable according to HTML spec
        Tag tag = Tag.valueOf("input");
        // When: checking form submittable property
        boolean result = tag.isFormSubmittable();
        // Then: expect true because <input> is in the formSubmit tags list (branch-true)
        assertTrue(result, "<input> should be considered form submittable");
    }

    @Test
    @DisplayName("isFormSubmittable returns false for a predefined non-submittable tag (div)")
    public void test_TC02() {
        // Given: a known HTML div tag, which is not submittable by HTML spec
        Tag tag = Tag.valueOf("div");
        // When: checking form submittable property
        boolean result = tag.isFormSubmittable();
        // Then: expect false because <div> is not in the formSubmit tags list (branch-false)
        assertFalse(result, "<div> should not be considered form submittable");
    }

    @Test
    @DisplayName("isFormSubmittable returns false for an unknown tag created at runtime (customTag)")
    public void test_TC03() {
        // Given: an unknown tag name, resulting in a runtime-created generic Tag
        Tag tag = Tag.valueOf("customTag");
        // When: checking form submittable property
        boolean result = tag.isFormSubmittable();
        // Then: expect false because custom tags default to non-submittable (branch-false)
        assertFalse(result, "Unknown tags should not be form submittable by default");
    }
}