package org.jsoup.parser;

import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class Tag_isFormSubmittable_2_Test {

    @Test
    @DisplayName("Uppercase known form-submit tag 'INPUT' with preserveTagCase=false returns static instance with formSubmit=true")
    public void test_TC07() {
        // Use uppercase "INPUT" and settings that do not preserve case so it matches the known registry key ("input"), driving the branch to reuse static tag
        String tagName = "INPUT";
        ParseSettings settings = ParseSettings.preserveLowerCase; // preserveTagCase = false
        // Invoke the valueOf path B0→B1→B2→B10→B11→B12
        Tag resultTag = Tag.valueOf(tagName, Parser.NamespaceHtml, settings);
        // The returned tag should be the same static instance for "input"
        Tag expected = Tag.valueOf("input", Parser.NamespaceHtml, settings);
        assertSame(expected, resultTag, "Expected to return the static registry instance for lowercase 'input'");
        // That static instance must be marked as form submittable
        assertTrue(resultTag.isFormSubmittable(), "Known input tag should be form submittable");
    }

    @Test
    @DisplayName("Null tagName in valueOf throws NullPointerException before isFormSubmittable")
    public void test_TC08() {
        // Passing null tagName to trigger Validate.notNull(tagName) precondition, driving B0→B1→exception
        String tagName = null;
        String namespace = Parser.NamespaceHtml;
        ParseSettings settings = ParseSettings.preserveCase;
        assertThrows(NullPointerException.class,
            () -> Tag.valueOf(tagName, namespace, settings),
            "Expected NullPointerException when tagName is null");
    }

    @Test
    @DisplayName("Blank tagName (\"   \") in valueOf throws IllegalArgumentException after trim")
    public void test_TC09() {
        // A blank string that trims to empty must fail Validate.notEmpty(tagName), driving B0→B1→B3→exception
        String tagName = "   ";
        String namespace = Parser.NamespaceHtml;
        ParseSettings settings = ParseSettings.preserveCase;
        assertThrows(IllegalArgumentException.class,
            () -> Tag.valueOf(tagName, namespace, settings),
            "Expected IllegalArgumentException when tagName is blank after trimming");
    }

    @Test
    @DisplayName("Null namespace in valueOf throws NullPointerException before isFormSubmittable")
    public void test_TC10() {
        // Valid tagName but null namespace triggers Validate.notNull(namespace), driving B0→B1→B2→exception
        String tagName = "input";
        String namespace = null;
        ParseSettings settings = ParseSettings.preserveCase;
        assertThrows(NullPointerException.class,
            () -> Tag.valueOf(tagName, namespace, settings),
            "Expected NullPointerException when namespace is null");
    }
}