package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for Tag.valueOf and isFormSubmittable scenarios (TC09, TC10, TC11).
 * Tests are self-contained and exercise specific branches as per scenarios.
 */
public class Tag_isFormSubmittable_2_Test {

    @Test
    @DisplayName("TC09: valueOf with preserveCase true and mixed-case tag triggers clone path before isFormSubmittable")
    public void test_TC09() {
        // GIVEN: mixed-case tagName not equal to normalName, settings preserve case
        String tagName = "DiV";
        String normalName = "div";
        String namespace = Parser.NamespaceHtml;
        ParseSettings settings = ParseSettings.preserveCase;
        // WHEN: call the full overload to hit the clone branch (tag exists by normalName, preserveTagCase true, tagName != normalName)
        Tag cloned = Tag.valueOf(tagName, normalName, namespace, settings);
        // THEN: should return a new instance (not the static one) with updated tagName, and formSubmittable false
        Tag staticDiv = Tag.valueOf(normalName, namespace, settings);
        assertNotSame(staticDiv, cloned, "Expected a cloned instance when preserveCase is true and name differs");
        assertEquals("DiV", cloned.getName(), "Cloned tag should preserve original case from input tagName");
        assertFalse(cloned.isFormSubmittable(), "Generic cloned div should not be form submittable");
    }

    @Test
    @DisplayName("TC10: valueOf with null ParseSettings throws NullPointerException before isFormSubmittable")
    public void test_TC10() {
        // GIVEN: valid tagName and namespace, but null settings to trigger NPE on settings.preserveTagCase()
        String tagName = "p";
        String namespace = Parser.NamespaceHtml;
        ParseSettings settings = null;
        // WHEN & THEN: NullPointerException is expected due to null settings
        assertThrows(NullPointerException.class,
            () -> Tag.valueOf(tagName, namespace, settings),
            "Expected NullPointerException when ParseSettings is null");
    }

    @Test
    @DisplayName("TC11: valueOf full-overload returns static known tag and isFormSubmittable true for 'input'")
    public void test_TC11() {
        // GIVEN: known input tag, normalName same, settings preserve case (but irrelevant since tag matches)
        String tagName = "input";
        String normalName = "input";
        String namespace = Parser.NamespaceHtml;
        ParseSettings settings = ParseSettings.preserveCase;
        // WHEN: call the full overload to hit the fast return branch for known tag
        Tag tagFull = Tag.valueOf(tagName, normalName, namespace, settings);
        // Control: retrieve default via simple overload
        Tag tagSimple = Tag.valueOf("input");
        // THEN: both references should be identical (static cache), and isFormSubmittable true for input
        assertSame(tagSimple, tagFull, "Expected same static Tag instance for known 'input' tag");
        assertTrue(tagFull.isFormSubmittable(), "Known 'input' tag should be form submittable");
    }
}