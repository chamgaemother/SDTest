package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.jsoup.parser.Parser; // Added import for Parser
import org.jsoup.parser.ParseSettings; // Added import for ParseSettings

public class Tag_isFormSubmittable_1_Test {

    @Test
    @DisplayName("Calling isFormSubmittable() on a cloned known form-submittable tag (uppercase OUTPUT with preserve-case) returns true")
    public void test_TC04() {
        // GIVEN: uppercase tagName that matches a known formSubmittable tag "output", with preserveTagCase=true
        String tagName = "OUTPUT";
        String namespace = Parser.NamespaceHtml;
        ParseSettings settings = ParseSettings.preserveCase; // preserveTagCase = true

        // WHEN: obtain Tag via valueOf, which should take the normalName lookup path, clone it, and preserve formSubmit flag
        Tag tag = Tag.valueOf(tagName, namespace, settings);

        // THEN: the clone path is taken (because preserveTagCase && uppercase doesn't equal normalName) and formSubmit should remain true
        // Inline justification: original "output" tag has formSubmit=true; clone must preserve that property
        assertTrue(tag.isFormSubmittable(), "Cloned tag for 'OUTPUT' in preserve-case mode should be form-submittable");
    }

    @Test
    @DisplayName("Calling isFormSubmittable() on a known form-submittable tag via normalName lookup (uppercase OPTION with lower-case settings) returns true")
    public void test_TC05() {
        // GIVEN: uppercase tagName that matches known formSubmittable tag "option", with preserveTagCase=false
        String tagName = "OPTION";
        String namespace = Parser.NamespaceHtml;
        ParseSettings settings = ParseSettings.notPreserveCase; // changed to use the correct static instance for not preserve case

        // WHEN: obtain Tag via valueOf, which should lowercase the tagName to "option" and fetch existing static tag without clone
        Tag tag = Tag.valueOf(tagName, namespace, settings);

        // THEN: direct normalName lookup path taken and formSubmit must be true
        // Inline justification: "option" is pre-registered with formSubmit=true; no clone path should be invoked here
        assertTrue(tag.isFormSubmittable(), "Known tag 'OPTION' in lowercase mode should be form-submittable");
    }
}