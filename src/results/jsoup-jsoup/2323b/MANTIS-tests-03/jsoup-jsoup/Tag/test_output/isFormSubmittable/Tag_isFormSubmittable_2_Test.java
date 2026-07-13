package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
public class Tag_isFormSubmittable_2_Test {

    private static final String HTML_NS = Parser.NamespaceHtml;
    private static final String MATHML_NS = Parser.NamespaceMathml;

    /**
     * Helper to access and clear the private static Tags map, then return it.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Tag> getAndClearTags() throws Exception {
        Field tagsField = Tag.class.getDeclaredField("Tags");
        tagsField.setAccessible(true);
        Map<String, Tag> tags = (Map<String, Tag>) tagsField.get(null);
        tags.clear();
        return tags;
    }

    /**
     * Helper to create a Tag instance via its private constructor.
     */
    private Tag newTag(String tagName, String normalName, String namespace) throws Exception {
        Constructor<Tag> ctor = Tag.class.getDeclaredConstructor(String.class, String.class, String.class);
        ctor.setAccessible(true);
        return ctor.newInstance(tagName, normalName, namespace);
    }

    @Test
    @DisplayName("valueOf with preserveTagCase=true and mismatched case triggers clone branch and tagName update")
    void test_TC06() throws Exception {
        // Precondition: only a known 'p' tag in html ns
        Map<String, Tag> tags = getAndClearTags();
        Tag baseP = newTag("p", "p", HTML_NS);
        tags.put("p", baseP);

        // Settings that preserve tag case so that tagName != normalName triggers clone + update
        ParseSettings settings = ParseSettings.preserveCase;
        String input = "P";
        String normal = ParseSettings.normalName(input);
        // WHEN
        Tag result = Tag.valueOf(input, normal, HTML_NS, settings);
        // THEN
        // result should not be the same instance as baseP, because case mismatch with preserveCase triggers clone
        assertNotSame(baseP, result, "Expected a cloned instance, not the original static tag");
        assertEquals("P", result.getName(), "Cloned tagName should reflect original casing input");
        assertEquals("p", result.normalName(), "normalName is always lowercase");
        assertFalse(result.isFormSubmittable(), "New cloned tag should have formSubmit=false by default");
    }

    @Test
    @DisplayName("valueOf with existing tagName match but namespace mismatch falls through to creation branch")
    void test_TC07() throws Exception {
        // Precondition: only a known 'div' tag in html ns
        Map<String, Tag> tags = getAndClearTags();
        Tag divTag = newTag("div", "div", HTML_NS);
        tags.put("div", divTag);

        // Settings that do not preserve case so path avoids clone path
        ParseSettings settings = new ParseSettings(false, false);
        String input = "div";
        String normal = ParseSettings.normalName(input);
        String customNs = "http://example.com/custom";

        // WHEN
        Tag result = Tag.valueOf(input, normal, customNs, settings);
        // THEN
        // Since namespace differs from existing divTag, a new Tag should be created
        assertNotSame(divTag, result, "Different namespace requires new Tag creation");
        assertEquals(customNs, result.namespace(), "Namespace should be the custom one provided");
        assertFalse(result.isFormSubmittable(), "New tag defaults to formSubmit=false");
    }

    @Test
    @DisplayName("valueOf returns predefined MathML tag without creation when namespace matches MathML entry")
    void test_TC08() throws Exception {
        // Precondition: only a known 'math' tag in MathML ns
        Map<String, Tag> tags = getAndClearTags();
        Tag mathTag = newTag("math", "math", MATHML_NS);
        tags.put("math", mathTag);

        ParseSettings settings = ParseSettings.preserveCase;
        String input = "math";
        String normal = ParseSettings.normalName(input);

        // WHEN
        Tag first = Tag.valueOf(input, normal, MATHML_NS, settings);
        Tag second = Tag.valueOf(input, normal, MATHML_NS, settings);
        // THEN
        // Both calls should return the exact same instance for predefined tag in correct namespace
        assertSame(mathTag, first, "First call should return the existing predefined instance");
        assertSame(first, second, "Second call should return the same cached instance");
        assertEquals(MATHML_NS, first.namespace(), "Namespace remains MathML for predefined tag");
    }

    @Test
    @DisplayName("valueOf with unknown tagName and default html namespace returns new tag and isFormSubmittable=false")
    void test_TC09() throws Exception {
        // Precondition: no 'newTag' in map
        Map<String, Tag> tags = getAndClearTags();
        // No insertion, so 'newTag' is unknown

        String input = "newTag";
        // WHEN using single-arg valueOf defaults to html namespace and default settings
        Tag result = Tag.valueOf(input);
        // THEN
        // Should create new Tag with the exact name and unread formSubmit flag
        assertEquals("newTag", result.getName(), "New tagName should match input exactly");
        assertFalse(result.isFormSubmittable(), "Unknown tags default to formSubmit=false");
    }
}