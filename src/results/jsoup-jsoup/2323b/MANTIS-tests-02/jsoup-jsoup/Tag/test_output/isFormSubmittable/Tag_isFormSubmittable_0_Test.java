package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUnit 5 tests for Tag.isFormSubmittable method based on provided scenarios.
 */
public class Tag_isFormSubmittable_0_Test {

    @Test
    @DisplayName("Calling isFormSubmittable() on a known form-submittable tag (input) returns true")
    public void test_TC01() {
        // Scenario TC01: 'input' is a pre-registered tag with formSubmit=true -> should return true (branch-true, known-tag, form-submittable)
        Tag tag = Tag.valueOf("input");
        boolean result = tag.isFormSubmittable();
        assertEquals(true, result, "Expected form-submittable known tag 'input' to return true");
    }

    @Test
    @DisplayName("Calling isFormSubmittable() on a known non-form-submittable tag (div) returns false")
    public void test_TC02() {
        // Scenario TC02: 'div' is a pre-registered tag with formSubmit=false -> should return false (branch-false, known-tag, non-form-submittable)
        Tag tag = Tag.valueOf("div");
        boolean result = tag.isFormSubmittable();
        assertEquals(false, result, "Expected non-form-submittable known tag 'div' to return false");
    }

    @Test
    @DisplayName("Calling isFormSubmittable() on an unknown tag returns false")
    public void test_TC03() {
        // Scenario TC03: 'customTag' is not pre-registered, default formSubmit=false -> should return false (branch-false, unknown-tag)
        Tag tag = Tag.valueOf("customTag");
        boolean result = tag.isFormSubmittable();
        assertEquals(false, result, "Expected unknown tag 'customTag' to default to non-form-submittable and return false");
    }
}