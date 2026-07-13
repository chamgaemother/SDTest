package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Tag.valueOf scenarios focusing on isFormSubmittable behavior.
 */
public class Tag_isFormSubmittable_2_Test {

    @Test
    @DisplayName("TC10: valueOf(\"TEXTAREA\", html, normal-case) returns the same static instance and isFormSubmittable=true")
    public void test_TC10() {
        // Scenario: Known tag "textarea" in HTML namespace, normal-case settings (no preserveCase).
        String tagName = "TEXTAREA";
        String namespace = org.jsoup.parser.Parser.Namespace.html;
        ParseSettings settings = ParseSettings.normal;
        
        // WHEN: call valueOf with uppercase name
        Tag result = Tag.valueOf(tagName, namespace, settings);
        
        // THEN: should return the canonical static textarea tag (no clone since namespace matches and preserveCase=false).
        Tag canonical = Tag.valueOf("textarea");
        assertSame(canonical, result, "Expected the original static instance for textarea, not a new one");
        assertTrue(result.isFormSubmittable(), "Expected textarea to be marked form submittable");
    }

    @Test
    @DisplayName("TC11: valueOf(\"input\", svg, normal-case) namespace mismatch yields new generic Tag and isFormSubmittable=false")
    public void test_TC11() {
        // Scenario: Known tag "input" but requesting SVG namespace causes namespace mismatch on both initial and secondary lookup.
        String tagName = "input";
        String namespace = org.jsoup.parser.Parser.Namespace.svg;
        ParseSettings settings = ParseSettings.normal;
        
        // WHEN: call valueOf with SVG namespace
        Tag result = Tag.valueOf(tagName, namespace, settings);
        
        // THEN: should create a new generic tag (not the HTML static instance) and not be form submittable
        Tag htmlInput = Tag.valueOf("input");
        assertNotSame(htmlInput, result, "Expected a new generic instance due to namespace mismatch");
        assertEquals("input", result.getName(), "Expected generic tag to retain provided tagName");
        assertFalse(result.isFormSubmittable(), "Expected generic SVG tag not to be marked form submittable");
    }

    @Test
    @DisplayName("TC12: valueOf(non-null tag, null namespace, any settings) throws NullPointerException at namespace check")
    public void test_TC12() {
        // Scenario: Null namespace should trigger a null-pointer check in Validate.notNull(namespace)
        String tagName = "div";
        String namespace = null;
        ParseSettings settings = ParseSettings.preserveCase;
        
        // WHEN / THEN: expect NullPointerException due to null namespace
        assertThrows(NullPointerException.class, () -> Tag.valueOf(tagName, namespace, settings),
            "Expected NullPointerException when namespace is null");
    }
}