package org.jsoup.parser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class Tag_isFormSubmittable_0_Test {

    @Test
    @DisplayName("isFormSubmittable() returns true when tag is known form-submittable (formSubmit==true)")
    public void test_TC01() {
        // GIVEN a known HTML input tag which is initialized in static setup with formSubmit = true
        Tag tag = Tag.valueOf("input");
        // WHEN checking if the tag is form-submittable
        boolean result = tag.isFormSubmittable();
        // THEN it should return true because 'input' is in the formSubmitTags list
        assertTrue(result, "Expected input tag to be form-submittable");
    }

    @Test
    @DisplayName("isFormSubmittable() returns false when tag is unknown (formSubmit==false)")
    public void test_TC02() {
        // GIVEN a custom unknown tag which is not in the formSubmitTags list and thus has formSubmit = false
        Tag tag = Tag.valueOf("customUnknown");
        // WHEN checking if the tag is form-submittable
        boolean result = tag.isFormSubmittable();
        // THEN it should return false because the unknown tag was not marked as submittable
        assertFalse(result, "Expected unknown tag to not be form-submittable");
    }
}