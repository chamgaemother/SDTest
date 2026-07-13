package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.parser.Tag;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Parser;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
public class Tag_isFormSubmittable_1_Test {

    @Test
    @DisplayName("Case-sensitive overload: known form-submittable tag ‘input’ returns true")
    public void test_TC03() {
        Tag tag = Tag.valueOf("input");
        boolean result = tag.isFormSubmittable();
        assertTrue(result, "Expected known tag 'input' to be submittable");
    }

    @Test
    @DisplayName("Overload with custom namespace: unknown tag returns false")
    public void test_TC04() {
        String tagName = "myCustom";
        String ns = Parser.NamespaceSvg;
        ParseSettings settings = ParseSettings.preserveCase;
        Tag tag = Tag.valueOf(tagName, ns, settings);
        boolean result = tag.isFormSubmittable();
        assertFalse(result, "Expected unknown tag 'myCustom' in SVG namespace to not be submittable");
    }

    @Test
    @DisplayName("Preserve-case clone path: uppercase known ‘INPUT’ returns true")
    public void test_TC05() throws Exception {
        Class<Tag> clazz = Tag.class;
        Method m = clazz.getDeclaredMethod("valueOf", String.class, String.class, String.class, ParseSettings.class);
        m.setAccessible(true);
        Object tagObj = m.invoke(null, "INPUT", "input", Parser.NamespaceHtml, ParseSettings.preserveCase);
        assertNotNull(tagObj, "Reflection invocation should return a Tag instance");
        assertTrue(tagObj instanceof Tag, "Returned object should be a Tag");
        Tag tag = (Tag) tagObj;
        boolean result = tag.isFormSubmittable();
        assertTrue(result, "Expected cloned tag for 'INPUT' to remain submittable");
    }
}