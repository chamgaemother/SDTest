package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;
public class Tag_isFormSubmittable_2_Test {

    @Test
    @DisplayName("TC08: valueOf(null,normal,ns) throws NullPointerException at Validate.notNull(tagName)")
    public void test_TC08() throws Exception {
        // Given a null tagName, expecting precondition validation to fail at notNull(tagName)
        String tagName = null;
        String normalName = "n";
        String namespace = Parser.NamespaceHtml;
        ParseSettings settings = ParseSettings.preserveCase;
        // When & Then: invoking the package-private valueOf should throw NullPointerException
        Method m = Tag.class.getDeclaredMethod(
            "valueOf", String.class, String.class, String.class, ParseSettings.class
        );
        m.setAccessible(true);
        assertThrows(NullPointerException.class, () -> {
            m.invoke(null, tagName, normalName, namespace, settings);
        });
    }

    @Test
    @DisplayName("TC09: valueOf(empty-string,normal,ns) throws IllegalArgumentException at Validate.notEmpty(tagName)")
    public void test_TC09() throws Exception {
        // Given a blank tagName (only whitespace), expecting notEmpty(tagName) validation to fail
        String tagName = "   ";
        String normalName = "n";
        String namespace = Parser.NamespaceHtml;
        ParseSettings settings = ParseSettings.preserveCase;
        Method m = Tag.class.getDeclaredMethod(
            "valueOf", String.class, String.class, String.class, ParseSettings.class
        );
        m.setAccessible(true);
        assertThrows(IllegalArgumentException.class, () -> {
            m.invoke(null, tagName, normalName, namespace, settings);
        });
    }

    @Test
    @DisplayName("TC10: valueOf(name,normal,null) throws NullPointerException at Validate.notNull(namespace)")
    public void test_TC10() throws Exception {
        // Given a null namespace, expecting notNull(namespace) validation to fail
        String tagName = "abc";
        String normalName = "abc";
        String namespace = null;
        ParseSettings settings = ParseSettings.preserveCase;
        Method m = Tag.class.getDeclaredMethod(
            "valueOf", String.class, String.class, String.class, ParseSettings.class
        );
        m.setAccessible(true);
        assertThrows(NullPointerException.class, () -> {
            m.invoke(null, tagName, normalName, namespace, settings);
        });
    }

    @Test
    @DisplayName("TC11: Uppercase known tag with preserveCase but tagName equals normalName returns static instance without clone")
    public void test_TC11() throws Exception {
        // Given a known tag 'div' in uppercase with preserveCase and tagName == normalName,
        // path should go to return of static Tags entry without cloning
        String tagName = "DIV";
        String normalName = "div";
        String namespace = Parser.NamespaceHtml;
        ParseSettings settings = ParseSettings.preserveCase;
        // Use reflection to call package-private valueOf
        Method m = Tag.class.getDeclaredMethod(
            "valueOf", String.class, String.class, String.class, ParseSettings.class
        );
        m.setAccessible(true);
        Tag result = (Tag) m.invoke(null, tagName, normalName, namespace, settings);
        // The static instance for "div" should be returned
        Tag expectedStatic = Tag.valueOf("div");
        assertSame(expectedStatic, result, "Expected the static 'div' tag instance without cloning");
        // The tag name should remain the normalized lower-case
        assertEquals("div", result.getName());
    }

    @Test
    @DisplayName("TC12: Known form-submittable tag 'button' returns true")
    public void test_TC12() {
        // Given the known HTML tag 'button', it is registered with formSubmit=true
        Tag tag = Tag.valueOf("button");
        // When checking form submittable
        boolean result = tag.isFormSubmittable();
        // Then it should return true for a form-submittable control
        assertTrue(result);
    }
}