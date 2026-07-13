package org.jsoup.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class Tag_isFormSubmittable_1_Test {

    @Test
    @DisplayName("isFormSubmittable() returns true for a known form-submittable tag with uppercase name triggering the clone branch")
    public void test_TC04() {
        // Arrange: uppercase tagName and preserve-case settings to force clone of the static Tag instance
        String tagName = "INPUT";
        String namespace = Parser.NamespaceHtml;
        ParseSettings settings = ParseSettings.preserveCase;
        
        // Act: get the Tag for "INPUT" which should clone the registered lowercase "input" tag
        Tag tag = Tag.valueOf(tagName, namespace, settings);
        boolean result = tag.isFormSubmittable();
        
        // Assert: a new clone was returned (not the same as the static registered Tag for "input")
        Tag original = Tag.valueOf("input", namespace, settings);
        assertNotSame(original, tag, "Expected a cloned Tag instance when requesting uppercase INPUT");
        // The clone should preserve the uppercase name
        assertEquals("INPUT", tag.getName(), "Cloned tagName should be the uppercase input");
        // The formSubmit flag should remain true for a known form-submittable tag
        assertTrue(result, "Expected isFormSubmittable() to return true for cloned INPUT tag");
    }

    @Test
    @DisplayName("isFormSubmittable() returns false when a known form-submittable tag is requested with a non-matching namespace leading to a new generic tag")
    public void test_TC05() {
        // Arrange: use correct tagName but wrong namespace, preserving case
        String tagName = "input";
        String wrongNamespace = "http://example.com/custom";
        ParseSettings settings = ParseSettings.preserveCase;
        
        // Act: get the Tag for "input" in the wrong namespace => should create a new generic Tag
        Tag tag = Tag.valueOf(tagName, wrongNamespace, settings);
        boolean result = tag.isFormSubmittable();
        
        // Assert: name remains as requested
        assertEquals("input", tag.getName(), "Generic tag should carry the original tagName");
        // The namespace should match the wrong one, verifying new instance
        assertEquals(wrongNamespace, tag.namespace(), "Namespace should be the custom namespace for the new Tag");
        // Default formSubmit should be false on a newly created generic tag
        assertFalse(result, "Expected isFormSubmittable() to return false for a generic tag with wrong namespace");
    }
}