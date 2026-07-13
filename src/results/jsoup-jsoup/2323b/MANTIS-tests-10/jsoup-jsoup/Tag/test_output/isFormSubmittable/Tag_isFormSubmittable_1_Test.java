package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for Tag.valueOf(String, String, ParseSettings) focusing on argument validation.
 */
public class Tag_isFormSubmittable_1_Test {

    @Test
    @DisplayName("TC06: valueOf(null, namespace, settings) throws IllegalArgumentException on null tagName")
    public void test_TC06() {
        // Branch B0->B1: tagName is null, should trigger Validate.notNull(tagName)
        String tagName = null;
        String namespace = Parser.NamespaceHtml;
        ParseSettings settings = ParseSettings.preserveCase;
        assertThrows(IllegalArgumentException.class, () -> {
            Tag.valueOf(tagName, namespace, settings);
        });
    }

    @Test
    @DisplayName("TC07: valueOf(\"   \", namespace, settings) throws IllegalArgumentException on empty tagName after trim")
    public void test_TC07() {
        // Branch B0->B1->B2: tagName trimmed to empty string, should trigger Validate.notEmpty(tagName)
        String tagName = "   ";
        String namespace = Parser.NamespaceHtml;
        ParseSettings settings = ParseSettings.preserveCase;
        assertThrows(IllegalArgumentException.class, () -> {
            Tag.valueOf(tagName, namespace, settings);
        });
    }

    @Test
    @DisplayName("TC08: valueOf(tagName, null, settings) throws IllegalArgumentException on null namespace")
    public void test_TC08() {
        // Branch B0->B1->B2->B3: namespace is null, should trigger Validate.notNull(namespace)
        String tagName = "input";
        String namespace = null;
        ParseSettings settings = ParseSettings.preserveCase;
        assertThrows(IllegalArgumentException.class, () -> {
            Tag.valueOf(tagName, namespace, settings);
        });
    }
}