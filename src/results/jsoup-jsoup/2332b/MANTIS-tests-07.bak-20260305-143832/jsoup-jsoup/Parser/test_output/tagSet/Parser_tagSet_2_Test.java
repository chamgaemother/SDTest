package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.parser.Parser;

import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_tagSet_2_Test {

    @Test
    @DisplayName("TC03: First reflection invocation of private static tagSet() initializes and returns non-empty tag set (init branch)")
    public void test_TC03() throws Exception {
        // Use reflection to access the private static method tagSet
        Class<?> cls = Parser.class;
        Method tagSetMethod = cls.getDeclaredMethod("tagSet");
        tagSetMethod.setAccessible(true);

        // Invoke the method for the first time: should initialize the static cache (B1→B2→B4)
        @SuppressWarnings("unchecked")
        Set<String> tags = (Set<String>) tagSetMethod.invoke(null);

        // The returned set must be non-null, non-empty, and contain known tag names per spec
        assertNotNull(tags, "The tag set should not be null on first invocation");
        assertFalse(tags.isEmpty(), "The tag set should be initialized and non-empty on first invocation");
        assertTrue(tags.contains("html"), "The initialized tag set should contain the 'html' tag");
    }

    @Test
    @DisplayName("TC04: Second reflection invocation of private static tagSet() returns cached instance without reinitializing (cache branch)")
    public void test_TC04() throws Exception {
        // Use reflection to access the private static method tagSet
        Class<?> cls = Parser.class;
        Method tagSetMethod = cls.getDeclaredMethod("tagSet");
        tagSetMethod.setAccessible(true);

        // First invocation populates the cache (B1→B2→B4)
        @SuppressWarnings("unchecked")
        Set<String> first = (Set<String>) tagSetMethod.invoke(null);

        // Second invocation should take the cache branch (B1→B3→B4) and return the same instance
        @SuppressWarnings("unchecked")
        Set<String> second = (Set<String>) tagSetMethod.invoke(null);

        // Assert that no new Set object was created and the same reference is returned
        assertSame(first, second, "Second invocation should return the same cached Set instance");
    }
}