package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
public class Parser_tagSet_2_Test {

    @Test
    @DisplayName("TC05: tagSet eliminates duplicate tags regardless of case, returning unique lowercase entries")
    public void test_TC05() throws Exception {
        // GIVEN: a parser with duplicate and mixed-case tag names, trackPosition=false
        Parser parser = Parser.htmlParser();
        ParseSettings settings = new ParseSettings(Arrays.asList("Div", "div", "SPAN", "span"), Locale.ROOT);
        parser.settings(settings);
        // WHEN: invoke private tagSet via reflection
        Method tagSetMethod = Parser.class.getDeclaredMethod("tagSet");
        tagSetMethod.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> result = (Set<String>) tagSetMethod.invoke(parser);
        // THEN: result.size() == 2 and contains only "div" and "span"
        assertEquals(2, result.size(),
                "Expected two unique tags after deduplication regardless of case");
        assertTrue(result.containsAll(Set.of("div", "span")),
                "Result should contain exactly 'div' and 'span' in lowercase");
    }

    @Test
    @DisplayName("TC06: tagSet lowercases using Turkish locale mapping (uppercase I → dotless ı)")
    public void test_TC06() throws Exception {
        // GIVEN: a parser with a single tag "I" and Turkish locale where uppercase I maps to dotless ı
        Parser parser = Parser.htmlParser();
        ParseSettings settings = new ParseSettings(Arrays.asList("I"), Locale.forLanguageTag("tr"));
        parser.settings(settings);
        // WHEN: invoke private tagSet via reflection to exercise locale-specific lowercase
        Method tagSetMethod = Parser.class.getDeclaredMethod("tagSet");
        tagSetMethod.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> result = (Set<String>) tagSetMethod.invoke(parser);
        // THEN: result must contain the Turkish dotless-i 'ı'
        assertEquals(1, result.size(),
                "Expected exactly one entry for the single tag");
        assertTrue(result.contains("ı"),
                "Expected locale-specific lowercase conversion of 'I' to Turkish dotless i");
    }

    @Test
    @DisplayName("TC07: tagSet returns lowercase tags when trackPosition=true and no nulls are present")
    public void test_TC07() throws Exception {
        // GIVEN: a parser with trackPosition enabled and settings with no nulls
        Parser parser = Parser.htmlParser().setTrackPosition(true);
        ParseSettings settings = new ParseSettings(Arrays.asList("P", "Span"), Locale.ROOT);
        parser.settings(settings);
        // WHEN: invoke private tagSet via reflection on trackPosition path
        Method tagSetMethod = Parser.class.getDeclaredMethod("tagSet");
        tagSetMethod.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> result = (Set<String>) tagSetMethod.invoke(parser);
        // THEN: tags are lowercased without exception
        assertEquals(Set.of("p", "span"), result,
                "Expected lowercase tags 'p' and 'span' when trackPosition enabled and no nulls");
    }
}