package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.jsoup.parser.Tag;
public class Tag_isFormSubmittable_0_Test {

    @Test
    @DisplayName("Known form-submittable tag 'input' returns true (formSubmit==true)")
    void test_TC01() {
        // 'input' is a predefined tag and listed in formSubmitTags, so formSubmit flag should be true
        Tag tag = Tag.valueOf("input");
        boolean result = tag.isFormSubmittable();
        assertEquals(true, result, "Expected 'input' to be form-submittable");
    }

    @Test
    @DisplayName("Known non-submittable tag 'div' returns false (formSubmit==false)")
    void test_TC02() {
        // 'div' is a predefined block tag but not in formSubmitTags, so formSubmit flag should be false
        Tag tag = Tag.valueOf("div");
        boolean result = tag.isFormSubmittable();
        assertEquals(false, result, "Expected 'div' to not be form-submittable");
    }

    @Test
    @DisplayName("Unknown tag 'custom' returns false (default formSubmit==false)")
    void test_TC03() {
        // 'custom' is not predefined, so a new Tag with default formSubmit=false should be returned
        Tag tag = Tag.valueOf("custom");
        boolean result = tag.isFormSubmittable();
        assertEquals(false, result, "Expected unknown tag 'custom' to not be form-submittable by default");
    }
}