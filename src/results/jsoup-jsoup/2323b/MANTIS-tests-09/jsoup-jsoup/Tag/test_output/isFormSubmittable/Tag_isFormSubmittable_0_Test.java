package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Tag_isFormSubmittable_0_Test {
    @Test
    @DisplayName("Known form-submittable tag 'input' returns true (formSubmit==true)")
    public void test_TC01() {
        // 'input' is registered in static init with formSubmit=true, so should be submittable
        Tag tag = Tag.valueOf("input");
        boolean result = tag.isFormSubmittable();
        assertTrue(result, "Expected 'input' tag to be form submittable");
    }

    @Test
    @DisplayName("Known non-form-submittable tag 'div' returns false (formSubmit==false)")
    public void test_TC02() {
        // 'div' is registered in static init with formSubmit=false, so should not be submittable
        Tag tag = Tag.valueOf("div");
        boolean result = tag.isFormSubmittable();
        assertFalse(result, "Expected 'div' tag not to be form submittable");
    }

    @Test
    @DisplayName("Unknown tag 'custom' returns false (generic tag formSubmit default false)")
    public void test_TC03() {
        // 'custom' is not in static registry, generic tag formSubmit defaults to false
        Tag tag = Tag.valueOf("custom");
        boolean result = tag.isFormSubmittable();
        assertFalse(result, "Expected unknown 'custom' tag not to be form submittable by default");
    }

    @Test
    @DisplayName("Case-sensitive overload with ParseSettings preserves case but formSubmit remains false for unknown tag")
    public void test_TC04() {
        // Using case-sensitive overload for 'MyTag'; not registered so generic, formSubmit should be false
        ParseSettings settings = ParseSettings.preserveCase;
        Tag tag = Tag.valueOf("MyTag", settings);
        boolean result = tag.isFormSubmittable();
        assertFalse(result, "Expected unknown case-preserved 'MyTag' tag not to be form submittable");
    }

    @Test
    @DisplayName("Invoking isFormSubmittable on null reference throws NullPointerException")
    public void test_TC05() {
        // Null tag reference should throw NullPointerException when calling isFormSubmittable
        Tag tag = null;
        assertThrows(NullPointerException.class, () -> {
            tag.isFormSubmittable();
        });
    }
}