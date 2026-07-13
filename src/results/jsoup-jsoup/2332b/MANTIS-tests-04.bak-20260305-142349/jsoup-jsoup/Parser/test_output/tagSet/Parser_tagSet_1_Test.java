package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_tagSet_1_Test {

    @Test
    @DisplayName("htmlParser.tagSet() returns non-empty set containing standard HTML5 tags")
    public void test_TC01() {
        // GIVEN a new HTML parser
        Parser parser = Parser.htmlParser();
        // WHEN obtaining the tag set via public API: should include standard HTML5 tags
        Set<String> tags = parser.tagSet(); // Updated method name
        // THEN the set is non-empty and contains common HTML tags
        assertAll("verify html5 tags",
            () -> assertTrue(tags.size() > 0, "Expected non-empty tag set for HTML parser"),
            () -> assertTrue(tags.contains("html"), "Expected 'html' tag in set"),
            () -> assertTrue(tags.contains("body"), "Expected 'body' tag in set"),
            () -> assertTrue(tags.contains("div"), "Expected 'div' tag in set")
        );
    }

    @Test
    @DisplayName("xmlParser.tagSet() returns an empty or minimal set since XML has no predefined HTML tags")
    public void test_TC02() {
        // GIVEN a new XML parser
        Parser parser = Parser.xmlParser();
        // WHEN obtaining the tag set via public API: XML parser should not have standard HTML5 tags
        Set<String> tags = parser.tagSet(); // Updated method name
        // THEN the set is empty or does not contain HTML5 tags like "html"
        boolean emptyOrNoHtml = tags.isEmpty() || !tags.contains("html");
        assertTrue(emptyOrNoHtml, "Expected XML parser tag set to be empty or not contain 'html'");
    }

    @Test
    @DisplayName("invoking private tagSet() by reflection without setAccessible throws IllegalAccessException")
    public void test_TC03() throws Exception {
        // GIVEN a Parser instance and a private tagSet method
        Parser parser = Parser.htmlParser();
        Method m = Parser.class.getDeclaredMethod("tagSet"); // Updated method name
        // DO NOT call m.setAccessible(true) to simulate illegal access
        // WHEN invoking the method reflectively
        assertThrows(IllegalAccessException.class, () -> {
            // attempt to invoke without proper access
            m.invoke(parser);
        });
    }

    @Test
    @DisplayName("invoking private tagSet() by reflection with setAccessible returns same as public htmlParser.tagSet()")
    public void test_TC04() throws Exception {
        // GIVEN a Parser instance and its private tagSet method made accessible
        Parser parser = Parser.htmlParser();
        Set<String> direct = parser.tagSet(); // Updated method name
        Method m = Parser.class.getDeclaredMethod("tagSet"); // Updated method name
        m.setAccessible(true);
        // WHEN invoking the private method reflectively
        @SuppressWarnings("unchecked")
        Set<String> reflect = (Set<String>) m.invoke(parser);
        // THEN the reflected result matches the direct call
        assertEquals(direct, reflect, "Expected reflective tagSet to return same set as direct call");
    }
}