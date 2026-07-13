package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUnit 5 tests for Tag.isFormSubmittable method based on provided scenarios.
 */
public class Tag_isFormSubmittable_0_Test {

    @Test
    @DisplayName("Known submittable tag 'input' returns true (formSubmit=true branch)")
    public void test_TC01() {
        // This input tag is predefined in static initialization to be form submittable (formSubmit=true)
        Tag tag = Tag.valueOf("input");
        boolean result = tag.isFormSubmittable();
        // Expect true because 'input' should be recognized as submittable control
        assertEquals(true, result);
    }

    @Test
    @DisplayName("Unknown tag 'mytag' returns false (formSubmit=false default branch)")
    public void test_TC02() {
        // 'mytag' is not a known tag, so it's created with formSubmit defaulting to false
        Tag tag = Tag.valueOf("mytag");
        boolean result = tag.isFormSubmittable();
        // Expect false because unknown tags should not be submittable by default
        assertEquals(false, result);
    }
}