package org.jsoup.parser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class Tag_isFormSubmittable_0_Test {

    @Test
    @DisplayName("Known form-submit tag \"input\" returns true (formSubmit==true)")
    public void test_TC01() {
        // TC01: 'input' is a pre-registered form-submit tag, so its formSubmit flag should be true (cover B1→B3)
        Tag tag = Tag.valueOf("input");
        boolean result = tag.isFormSubmittable();
        assertTrue(result, "Expected known form-submit tag 'input' to return true");
    }

    @Test
    @DisplayName("Known non-submit tag \"div\" returns false (formSubmit==false)")
    public void test_TC02() {
        // TC02: 'div' is a block tag without formSubmit capability, so its formSubmit flag should be false (cover B1→B2)
        Tag tag = Tag.valueOf("div");
        boolean result = tag.isFormSubmittable();
        assertFalse(result, "Expected known non-submit tag 'div' to return false");
    }

    @Test
    @DisplayName("Unknown custom tag \"foo\" returns false (default formSubmit==false)")
    public void test_TC03() {
        // TC03: 'foo' is not a predefined tag, new generic tags default formSubmit to false (cover B1→B2)
        Tag tag = Tag.valueOf("foo");
        boolean result = tag.isFormSubmittable();
        assertFalse(result, "Expected unknown tag 'foo' to return false by default");
    }
}