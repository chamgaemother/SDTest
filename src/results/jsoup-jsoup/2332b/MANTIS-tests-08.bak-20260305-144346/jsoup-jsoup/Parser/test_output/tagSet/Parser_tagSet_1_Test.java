package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Parser.tagSet method. Covers null input, empty input,
 * single token, multiple tokens with whitespace trimming, and duplicate elimination.
 */
public class Parser_tagSet_1_Test {

    @Test
    @DisplayName("tagSet(nullptr) throws NullPointerException")
    public void test_TC01() {
        // GIVEN: a null input should trigger the null-input check path (B0->B1)
        String input = null;
        // WHEN & THEN: calling tagSet with null must throw NullPointerException
        assertThrows(NullPointerException.class, () -> Parser.tagSet(input));
    }

    @Test
    @DisplayName("tagSet(\"") returns an empty set (empty-input branch)")
    public void test_TC02() {
        // GIVEN: empty string triggers empty-input branch (B0->B2->B5)
        String input = "";
        // WHEN: parse empty input
        Set<String> result = Parser.tagSet(input);
        // THEN: expect an empty set
        assertTrue(result.isEmpty(), "Expected empty set for empty input");
    }

    @Test
    @DisplayName("tagSet(\"a\") returns Set containing single tag (no-split loop zero-iterations)")
    public void test_TC03() {
        // GIVEN: single token with no commas so loop executes once but no split parts (B0->B3->B6->B5)
        String input = "a";
        // WHEN: parse single token
        Set<String> result = Parser.tagSet(input);
        // THEN: result should contain exactly one element "a"
        assertEquals(1, result.size(), "Expected size 1 for single token");
        assertTrue(result.contains("a"), "Expected set to contain 'a'");
    }

    @Test
    @DisplayName("tagSet(\"a,b, c\") splits on commas and trims whitespace (loop multiple iterations)")
    public void test_TC04() {
        // GIVEN: input with multiple comma-separated tokens and whitespace to trigger trimming (B0->B3->B6 x3->B5)
        String input = "a,b, c";
        // WHEN: parse multiple tokens
        Set<String> result = Parser.tagSet(input);
        // THEN: result should contain exactly "a","b","c"
        assertEquals(3, result.size(), "Expected three unique tokens");
        assertTrue(result.containsAll(Arrays.asList("a", "b", "c")),
                "Expected set to contain 'a', 'b', and 'c'");
    }

    @Test
    @DisplayName("tagSet(\"a,a,b\") returns unique tags only (duplicate elimination branch)")
    public void test_TC05() {
        // GIVEN: input with duplicates to trigger duplicate-elimination (B0->B3->B6 x3->B7->B5)
        String input = "a,a,b";
        // WHEN: parse input with duplicates
        Set<String> result = Parser.tagSet(input);
        // THEN: duplicates removed, expect exactly "a" and "b"
        assertEquals(2, result.size(), "Expected two unique tokens after duplicate elimination");
        assertTrue(result.containsAll(Arrays.asList("a", "b")),
                "Expected set to contain 'a' and 'b' only");
    }
}