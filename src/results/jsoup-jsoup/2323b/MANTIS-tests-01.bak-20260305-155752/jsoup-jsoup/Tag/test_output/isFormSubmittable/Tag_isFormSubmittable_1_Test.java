package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for org.jsoup.parser.Tag.isFormSubmittable method based on provided scenarios.
 */
public class Tag_isFormSubmittable_1_Test {

    @Test
    @DisplayName("Known form-submit tag “option” returns true via valueOf(String,ParseSettings) overload")
    public void test_TC03() {
        // GIVEN a form-submit tag "option" and settings preserving case
        ParseSettings settings = ParseSettings.preserveCase;
        // WHEN retrieving via the overload that uses HTML namespace by default
        Tag t = Tag.valueOf("option", settings);
        // THEN it should be recognized as form-submittable
        assertTrue(t.isFormSubmittable(), "Expected 'option' to be form-submittable in HTML namespace");
    }

    @Test
    @DisplayName("Known form-submit tag “input” in non-HTML namespace returns false via valueOf(String, String, ParseSettings)")
    public void test_TC04() {
        // GIVEN a form-submit tag "input" but using SVG namespace, which mismatches predefined HTML mapping
        String ns = Parser.NamespaceSvg;
        ParseSettings settings = ParseSettings.preserveCase;
        // WHEN retrieving tag in SVG namespace
        Tag t = Tag.valueOf("input", ns, settings);
        // THEN it should not be recognized as form-submittable because namespace mismatch
        assertFalse(t.isFormSubmittable(), "Expected 'input' in SVG namespace not to be form-submittable");
    }

    @Test
    @DisplayName("Uppercase known form-submit tag triggers clone path and preserves formSubmit=true")
    public void test_TC05() {
        // GIVEN uppercase name of known form-submit tag and settings preserve case
        String name = "INPUT";
        String normal = ParseSettings.normalName(name);
        String ns = Parser.NamespaceHtml;
        ParseSettings settings = ParseSettings.preserveCase;
        // Base instance from lowercase default retrieval
        Tag base = Tag.valueOf("input");
        // WHEN retrieving with uppercase name, forcing clone path when preserveTagCase=true
        Tag t = Tag.valueOf(name, normal, ns, settings);
        // THEN the returned Tag should be a clone (not same static instance)
        // and should preserve formSubmit property = true
        assertNotSame(base, t, "Expected a new cloned instance for uppercase 'INPUT'");
        assertTrue(t.isFormSubmittable(), "Expected cloned 'INPUT' to preserve form-submittable=true");
    }
}