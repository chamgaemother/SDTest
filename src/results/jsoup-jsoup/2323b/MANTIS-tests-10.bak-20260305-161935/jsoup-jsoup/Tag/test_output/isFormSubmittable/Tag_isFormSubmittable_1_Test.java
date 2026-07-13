package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
public class Tag_isFormSubmittable_1_Test {

    @Test
    @DisplayName("Known form-submit tag \"option\" in default HTML namespace returns true")
    public void test_TC04() {
        // Given: 'option' is a known form-submit tag in HTML namespace
        String tagName = "option";
        // When: retrieving tag via valueOf default settings (HTML namespace, preserve case irrelevant)
        Tag tag = Tag.valueOf(tagName);
        boolean result = tag.isFormSubmittable();
        // Then: expected true because 'option' is in SharedConstants.FormSubmitTags for HTML
        assertEquals(true, result);
    }

    @Test
    @DisplayName("Known form-submit tag \"option\" in non-HTML namespace returns false")
    public void test_TC05() {
        // Given: 'option' is a known form-submit tag, but using SVG namespace should yield a generic tag
        String tagName = "option";
        String namespace = Parser.NamespaceSvg;
        ParseSettings settings = ParseSettings.preserveCase;
        // When: retrieving tag in SVG namespace
        Tag tag = Tag.valueOf(tagName, namespace, settings);
        boolean result = tag.isFormSubmittable();
        // Then: expected false because generic tags in non-HTML namespace do not preserve formSubmit flag
        assertEquals(false, result);
    }

    @Test
    @DisplayName("Known form-submit tag with case-preserve settings and uppercase name triggers clone branch and returns true")
    public void test_TC06() {
        // Given: 'INPUT' uppercase and preserveCase=true should trigger clone of existing input tag
        String tagName = "INPUT";
        String namespace = Parser.NamespaceHtml;
        ParseSettings settings = ParseSettings.preserveCase;
        
        // When: retrieving tag will go through clone path since preserveCase and name differs
        Tag tag = Tag.valueOf(tagName, namespace, settings);
        boolean result = tag.isFormSubmittable();
        // Then: expected true because cloned INPUT should retain formSubmit capability
        assertEquals(true, result);
    }
}