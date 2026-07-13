package org.jsoup.parser;

import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_tagSet_2_Test {

    @Test
    @DisplayName("TC05: parser.tagSet() returns empty set when underlying TreeBuilder provides no tags")
    public void test_TC05() throws Exception {
        // Arrange: stub TreeBuilder to return empty defaultTagSet, so B0->B2 override branch
        class EmptyBuilder extends HtmlTreeBuilder {
            @Override protected Set<String> defaultTagSet() {
                return Collections.emptySet();
            }
        }
        Parser parser = new Parser(new EmptyBuilder());
        // Use reflection to access private tagSet method
        Method m = Parser.class.getDeclaredMethod("tagSet");
        m.setAccessible(true);
        // Act: invoke tagSet, should return empty set (override defaultTagSet -> empty)
        @SuppressWarnings("unchecked")
        Set<String> tags = (Set<String>) m.invoke(parser);
        // Assert: expected empty set
        assertTrue(tags.isEmpty(), "Expected empty tag set when TreeBuilder.defaultTagSet() is empty");
    }

    @Test
    @DisplayName("TC06: parser.tagSet() returns uppercase tags when case-preserving settings applied")
    public void test_TC06() throws Exception {
        // Arrange: use default HTML parser (HtmlTreeBuilder) => B0->B1
        Parser parser = Parser.htmlParser();
        // Apply case-preserving settings to trigger B3(casePreserve) path
        parser.settings(ParseSettings.preserveCase);
        // Access private tagSet
        Method m = Parser.class.getDeclaredMethod("tagSet");
        m.setAccessible(true);
        // Act: invoke tagSet
        @SuppressWarnings("unchecked")
        Set<String> tags = (Set<String>) m.invoke(parser);
        // Assert: uppercase tag names should be present
        assertTrue(tags.contains("HTML"), "Expected uppercase 'HTML' when preserveCase is set");
        assertTrue(tags.contains("BODY"), "Expected uppercase 'BODY' when preserveCase is set");
    }

    @Test
    @DisplayName("TC07: private tagSet() reflectively invoked on XML parser returns same set as public call")
    public void test_TC07() throws Exception {
        // Arrange: create XML parser to follow B1(xml) path
        Parser parser = Parser.xmlParser();
        // Reflectively get private tagSet method with accessibility for direct call
        Method m = Parser.class.getDeclaredMethod("tagSet");
        m.setAccessible(true);
        // Act: invoke twice to simulate direct vs reflective usage (both now accessible)
        @SuppressWarnings("unchecked")
        Set<String> direct = (Set<String>) m.invoke(parser);
        @SuppressWarnings("unchecked")
        Set<String> reflect = (Set<String>) m.invoke(parser);
        // Assert: both invocations yield identical results
        assertEquals(direct, reflect, "Reflective invocation should return same tag set as direct call");
    }

    @Test
    @DisplayName("TC08: reflective invocation without accessibility throws IllegalAccessException for private tagSet")
    public void test_TC08() throws Exception {
        // Arrange: HTML parser (HtmlTreeBuilder) to follow B1(html) path
        Parser parser = Parser.htmlParser();
        Method m = Parser.class.getDeclaredMethod("tagSet");
        // Do NOT call setAccessible(true) to trigger IllegalAccessException
        // Act & Assert: invoking without accessibility should throw IllegalAccessException
        assertThrows(IllegalAccessException.class, () -> {
            try {
                m.invoke(parser);
            } catch (InvocationTargetException ite) {
                // Unwrap if underlying method threw
                throw ite.getCause();
            }
        });
    }
}