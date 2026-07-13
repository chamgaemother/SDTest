package org.jsoup.parser;

import org.jsoup.parser.Tag;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class Tag_isFormSubmittable_2_Test {

    @Test
    @DisplayName("Uppercase known form-submittable tag via lower-case settings returns true without clone")
    public void test_TC05() {
        String tagName = "OPTION";
        ParseSettings settings = ParseSettings.notPreserveCase; // preserveTagCase = false
        Tag tag = Tag.valueOf(tagName, settings);
        assertTrue(tag.isFormSubmittable(), "Expected known 'option' tag to be form submittable with lower-case settings");
    }

    @Test
    @DisplayName("Known tag in wrong namespace returns new generic tag with formSubmit=false")
    public void test_TC06() {
        String tagName = "input";
        String namespace = "customNS";
        ParseSettings settings = ParseSettings.preserveCase; // preserveTagCase = true
        Tag tag = Tag.valueOf(tagName, namespace, settings);
        assertFalse(tag.isFormSubmittable(), "Expected generic tag in wrong namespace to have formSubmit=false");
    }

    @Test
    @DisplayName("Overload valueOf(name,namespace,settings) clone path preserves formSubmit flag for uppercase OUTPUT with preserve-case")
    public void test_TC07() {
        String tagName = "OUTPUT";
        String namespace = Parser.NamespaceHtml;
        ParseSettings settings = ParseSettings.preserveCase; // preserveTagCase = true
        Tag tag = Tag.valueOf(tagName, namespace, settings);
        assertTrue(tag.isFormSubmittable(), "Expected clone of known 'output' tag to preserve formSubmit=true");
    }

    @Test
    @DisplayName("Null tagName parameter throws IllegalArgumentException")
    public void test_TC08() {
        String tagName = null;
        String namespace = Parser.NamespaceHtml;
        ParseSettings settings = ParseSettings.preserveCase;
        assertThrows(IllegalArgumentException.class,
            () -> Tag.valueOf(tagName, namespace, settings),
            "Expected IllegalArgumentException when tagName is null");
    }
}