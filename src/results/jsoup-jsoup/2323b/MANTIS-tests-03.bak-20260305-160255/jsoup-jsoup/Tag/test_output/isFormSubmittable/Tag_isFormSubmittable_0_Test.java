package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUnit 5 tests for Tag.isFormSubmittable method based on provided scenarios.
 */
public class Tag_isFormSubmittable_0_Test {

    @Test
    @DisplayName("Known form-submittable tag 'input' returns true (formSubmit==true)")
    public void test_TC01() {
        // 'input' is a predefined form control that should be marked as submittable (formSubmit == true)
        Tag tag = Tag.valueOf("input");
        boolean result = tag.isFormSubmittable();
        assertEquals(true, result, "Expected 'input' tag to be form submittable");
    }

    @Test
    @DisplayName("Unknown tag 'custom' returns false (default formSubmit==false)")
    public void test_TC02() {
        // 'custom' is not a predefined tag, so default formSubmit flag should be false
        Tag tag = Tag.valueOf("custom");
        boolean result = tag.isFormSubmittable();
        assertEquals(false, result, "Expected unknown 'custom' tag to not be form submittable");
    }
}