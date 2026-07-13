package org.jsoup.parser;

import org.jsoup.parser.Tag;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for Tag.isFormSubmittable and related Tag.valueOf behavior.
 */
public class Tag_isFormSubmittable_1_Test {

    @Test
    @DisplayName("TC04: Known submittable tag 'input' in wrong namespace returns false for formSubmit flag")
    public void test_TC04() {
        String tagName = "input";
        String namespace = Parser.NamespaceSvg;
        ParseSettings settings = ParseSettings.preserveCase;

        Tag tag = Tag.valueOf(tagName, namespace, settings);

        assertFalse(tag.isFormSubmittable(), "Expected formSubmit=false for 'input' in wrong namespace");
        assertEquals(tagName, tag.getName(), "Tag name should be preserved as given");
    }

    @Test
    @DisplayName("TC05: Uppercase 'INPUT' with preserveCase triggers clone path and returns true for formSubmit flag")
    public void test_TC05() {
        String tagName = "INPUT";
        ParseSettings settings = ParseSettings.preserveCase;

        Tag tag = Tag.valueOf(tagName, settings);

        assertTrue(tag.isFormSubmittable(), "Expected formSubmit=true for cloned 'INPUT'");
        assertEquals("INPUT", tag.getName(), "Cloned tag should preserve the uppercase name");
    }

    @Test
    @DisplayName("TC06: Calling valueOf with null tagName throws IllegalArgumentException")
    public void test_TC06() {
        String tagName = null;
        String normalName = "x";
        String namespace = Parser.NamespaceHtml;
        ParseSettings settings = ParseSettings.preserveCase;

        assertThrows(IllegalArgumentException.class, () -> {
            Tag.valueOf(tagName, normalName, namespace, settings);
        }, "Expected IllegalArgumentException when tagName is null");
    }

    @Test
    @DisplayName("TC07: Calling valueOf with empty tagName throws IllegalArgumentException")
    public void test_TC07() {
        String tagName = "   ";
        String normalName = "n";
        String namespace = Parser.NamespaceHtml;
        ParseSettings settings = ParseSettings.preserveCase;

        assertThrows(IllegalArgumentException.class, () -> {
            Tag.valueOf(tagName, normalName, namespace, settings);
        }, "Expected IllegalArgumentException when tagName is empty after trimming");
    }
}