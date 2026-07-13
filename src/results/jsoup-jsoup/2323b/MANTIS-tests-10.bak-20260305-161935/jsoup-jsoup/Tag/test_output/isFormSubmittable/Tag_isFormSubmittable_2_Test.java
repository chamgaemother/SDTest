package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class Tag_isFormSubmittable_2_Test {

    @Test
    @DisplayName("TC07: valueOf(null, namespace, settings) throws IllegalArgumentException for null tagName")
    void test_TC07() {
        // Passing null tagName should trigger Validate.notNull and throw IllegalArgumentException
        String tagName = null;
        String namespace = Parser.NamespaceHtml;
        ParseSettings settings = ParseSettings.preserveCase;
        assertThrows(IllegalArgumentException.class, () -> {
            Tag.valueOf(tagName, namespace, settings);
        });
    }

    @Test
    @DisplayName("TC08: valueOf(\"   \", namespace, settings) throws IllegalArgumentException for empty/blank tagName after trim")
    void test_TC08() {
        // Blank tagName (only whitespace) should be trimmed to empty and trigger Validate.notEmpty
        String tagName = "   ";
        String namespace = Parser.NamespaceHtml;
        ParseSettings settings = ParseSettings.preserveCase;
        assertThrows(IllegalArgumentException.class, () -> {
            Tag.valueOf(tagName, namespace, settings);
        });
    }

    @Test
    @DisplayName("TC09: valueOf(\"INPUT\", default namespace, non-preserve settings) returns known tag without clone and formSubmit==true")
    void test_TC09() {
        // Uppercase "INPUT" with preserveTagCase=false should normalize to "input" and pick the static known tag,
        // which is formSubmittable since "input" is in SharedConstants.FormSubmitTags.
        String tagName = "INPUT";
        String namespace = Parser.NamespaceHtml;
        ParseSettings settings = ParseSettings.lowerCase; // preserveTagCase() == false
        Tag tag = Tag.valueOf(tagName, namespace, settings);
        assertAll(
            () -> assertEquals("input", tag.getName(), "Tag name should be normalized to lowercase 'input'"),
            () -> assertTrue(tag.isFormSubmittable(), "Known input tag should be form submittable")
        );
    }

    @Test
    @DisplayName("TC10: valueOf(String, ParseSettings) overload for known form-submit tag returns true")
    void test_TC10() {
        // Using the overload valueOf(tagName, settings) for "option" should return the known tag,
        // and option is in form-submit list so isFormSubmittable() must be true.
        String tagName = "option";
        String namespace = Parser.NamespaceHtml;
        ParseSettings settings = ParseSettings.preserveCase;
        Tag tag = Tag.valueOf(tagName, namespace, settings);
        boolean result = tag.isFormSubmittable();
        assertTrue(result, "Option tag should be form submittable");
    }
}