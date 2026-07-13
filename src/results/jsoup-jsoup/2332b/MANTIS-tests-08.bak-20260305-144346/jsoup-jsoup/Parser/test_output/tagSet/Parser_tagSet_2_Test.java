package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_tagSet_2_Test {

    @Test
    @DisplayName("TC06: tagSet skips empty tokens when input contains only commas and whitespace")
    public void test_TC06() throws Exception {
        // This input contains only delimiters and whitespace, so all tokens are empty and should be skipped.
        String input = ", , ,";
        // Use reflection to access the static tagSet method
        Method tagSetMethod = Parser.class.getDeclaredMethod("tagSet", String.class);
        tagSetMethod.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> result = (Set<String>) tagSetMethod.invoke(null, input);
        assertTrue(result.isEmpty(), "Expected an empty set when input contains only commas and whitespace");
    }

    @Test
    @DisplayName("TC07: tagSet lower-cases uppercase tokens before adding to the set")
    public void test_TC07() throws Exception {
        // The tokens "DIV" and "Span" are non-empty and valid; they are converted to lower-case.
        String input = "DIV,Span";
        Method tagSetMethod = Parser.class.getDeclaredMethod("tagSet", String.class);
        tagSetMethod.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> result = (Set<String>) tagSetMethod.invoke(null, input);
        // Expect exactly two entries in lower-case
        assertAll("Check that both tags are normalized to lower-case and included",
            () -> assertEquals(2, result.size(), "Should contain two tags"),
            () -> assertTrue(result.contains("div"), "Should contain 'div'"),
            () -> assertTrue(result.contains("span"), "Should contain 'span'")
        );
    }

    @Test
    @DisplayName("TC08: tagSet throws IllegalArgumentException for tokens with illegal characters")
    public void test_TC08() throws Exception {
        // The second token contains an illegal character '@' triggering the exception path.
        String input = "valid, inv@lid";
        Method tagSetMethod = Parser.class.getDeclaredMethod("tagSet", String.class);
        tagSetMethod.setAccessible(true);
        // Expect an IllegalArgumentException due to invalid token
        assertThrows(IllegalArgumentException.class, () -> {
            tagSetMethod.invoke(null, input);
        }, "Expected IllegalArgumentException for token containing illegal characters");
    }
}