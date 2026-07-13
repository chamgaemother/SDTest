package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
public class Parser_tagSet_2_Test {

    @Test
    @DisplayName("Calling clear() on returned tagSet throws UnsupportedOperationException (unmodifiable path)")
    public void test_TC04() throws Exception {
        // Reset static cache via reflection to force fresh initialization (covers B0→B1→B2)
        Field cacheField = Parser.class.getDeclaredField("tagSet");
        cacheField.setAccessible(true);
        cacheField.set(null, null);
        // First invocation initializes the cache (B3)
        Set<String> tags = Parser.tagSet(); // Changed back to tagSet()
        // Attempting mutation should hit unmodifiable wrapper (B4)
        assertThrows(UnsupportedOperationException.class, () -> {
            // clear() should be unsupported
            tags.clear();
        });
    }

    @Test
    @DisplayName("Calling addAll() on returned tagSet throws UnsupportedOperationException (unmodifiable path)")
    public void test_TC05() throws Exception {
        // Reset static cache via reflection to force fresh initialization (covers B0→B1→B2)
        Field cacheField = Parser.class.getDeclaredField("tagSet");
        cacheField.setAccessible(true);
        cacheField.set(null, null);
        // First invocation initializes the cache (B3)
        Set<String> tags = Parser.tagSet(); // Changed back to tagSet()
        // Prepare dummy set to attempt bulk mutation
        Set<String> dummy = Collections.singleton("dummyTag");
        // bulk mutation should hit unmodifiable wrapper (B4)
        assertThrows(UnsupportedOperationException.class, () -> {
            tags.addAll(dummy);
        });
    }
}