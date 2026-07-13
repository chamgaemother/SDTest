package org.jsoup.parser;

import org.jsoup.parser.Tag;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Tag_isFormSubmittable_1_Test {

    @Test
    @DisplayName("TC04: valueOf(null, …) throws NullPointerException at Validate.notNull(tagName)")
    public void test_TC04() {
        // GIVEN a null tagName should trigger Validate.notNull
        String tagName = null;
        String namespace = "html";
        ParseSettings settings = ParseSettings.preserveCase;
        // WHEN & THEN expect NullPointerException due to null tagName
        assertThrows(NullPointerException.class, () -> Tag.valueOf(tagName, namespace, settings));
    }

    @Test
    @DisplayName("TC05: valueOf(\"  \", …) throws IllegalArgumentException for empty tagName after trim")
    public void test_TC05() {
        // GIVEN a blank tagName that trims to empty
        String tagName = "   ";
        String namespace = "html";
        ParseSettings settings = ParseSettings.preserveCase;
        // WHEN & THEN expect IllegalArgumentException due to empty tagName after trim
        assertThrows(IllegalArgumentException.class, () -> Tag.valueOf(tagName, namespace, settings));
    }

    @Test
    @DisplayName("TC06: valueOf(\"TEXTAREA\", html, normal-case) returns direct known tag without cloning")
    public void test_TC06() {
        // GIVEN a known submit tag in uppercase and settings normal (case-insensitive)
        String tagName = "TEXTAREA";
        // WHEN retrieving tag with normal settings
        Tag result = Tag.valueOf(tagName, Parser.NamespaceHtml, ParseSettings.normal);
        // THEN should return the static instance for lowercase "textarea"
        Tag expected = Tag.valueOf("textarea");
        assertSame(expected, result, "Expected same static instance for known tag TEXTAREA under normal parse settings");
        // AND isFormSubmittable should be true for textarea
        assertTrue(result.isFormSubmittable(), "Expected textarea to be form submittable");
    }

    @Test
    @DisplayName("TC07: valueOf(\"DIV\", html, preserve-case) clones known non-submit tag and preserves case")
    public void test_TC07() {
        // GIVEN a known non-submit tag in uppercase with preserveCase true
        String tagName = "DIV";
        ParseSettings settings = ParseSettings.preserveCase;
        // WHEN retrieving the tag
        Tag result = Tag.valueOf(tagName, Parser.NamespaceHtml, settings);
        // THEN result should not be the static lowercase "div" instance (clone occurred)
        Tag staticDiv = Tag.valueOf("div");
        assertNotSame(staticDiv, result, "Expected a cloned instance when preserving case for DIV");
        // AND the tag name preserved
        assertEquals("DIV", result.getName(), "Cloned tag should preserve uppercase name");
        // AND non-submit tags should report false
        assertFalse(result.isFormSubmittable(), "Expected DIV to not be form submittable");
    }

    @Test
    @DisplayName("TC08: valueOf(\"input\", svg, normal-case) yields new generic tag for namespace mismatch")
    public void test_TC08() {
        // GIVEN a known tag "input" but with mismatched namespace SVG
        String tagName = "input";
        String namespace = Parser.NamespaceSvg;
        ParseSettings settings = ParseSettings.normal;
        // WHEN retrieving tag
        Tag result = Tag.valueOf(tagName, namespace, settings);
        // THEN should create a generic tag (not registered), name remains input
        assertEquals("input", result.getName(), "Generic tag should keep provided tagName");
        // AND namespace mismatch generic tags are not form-submittable by default
        assertFalse(result.isFormSubmittable(), "Generic input in SVG namespace should not be form submittable");
    }

    @Test
    @DisplayName("TC09: valueOf(\"  input  \", default) trims whitespace before lookup and returns known submit tag")
    public void test_TC09() {
        // GIVEN a tagName with surrounding whitespace
        String raw = "  input  ";
        // WHEN using default valueOf overload (HTML namespace, preserveCase default)
        Tag result = Tag.valueOf(raw);
        // THEN whitespace should be trimmed and static input returned
        assertEquals("input", result.getName(), "Whitespace should be trimmed to lookup known tag");
        // AND input is form submittable
        assertTrue(result.isFormSubmittable(), "Input should be form submittable in HTML namespace");
    }
}