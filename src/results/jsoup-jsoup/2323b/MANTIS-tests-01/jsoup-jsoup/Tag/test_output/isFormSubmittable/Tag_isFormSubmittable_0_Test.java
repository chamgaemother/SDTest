package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Tag_isFormSubmittable_0_Test {

    @Test
    @DisplayName("Built-in 'input' tag is form submittable (formSubmit == true)")
    public void test_TC01() {
        // This input is a known form submittable element (path B0→B1 where formSubmit flag true)
        Tag t = Tag.valueOf("input");
        boolean result = t.isFormSubmittable();
        assertTrue(result, "Expected built-in 'input' tag to be form submittable");
    }

    @Test
    @DisplayName("Built-in 'div' tag is not form submittable (formSubmit == false)")
    public void test_TC02() {
        // The 'div' tag is a known block element but not submittable (path B0→B1 where formSubmit flag false)
        Tag t = Tag.valueOf("div");
        boolean result = t.isFormSubmittable();
        assertFalse(result, "Expected built-in 'div' tag to not be form submittable");
    }

    @Test
    @DisplayName("Unknown tag 'custom' is not form submittable by default (formSubmit == false)")
    public void test_TC03() {
        // 'custom' is not predefined and default formSubmit is false for unknown tags
        Tag t = Tag.valueOf("custom");
        boolean result = t.isFormSubmittable();
        assertFalse(result, "Expected unknown 'custom' tag to not be form submittable by default");
    }
}