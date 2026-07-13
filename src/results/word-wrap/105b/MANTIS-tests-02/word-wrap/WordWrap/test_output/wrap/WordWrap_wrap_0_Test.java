package org.davidmoten.text.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class WordWrap_wrap_0_Test {

    @Test
    @DisplayName("wrap() returns empty string when source is empty (loop zero iterations)")
    public void test_TC01() {
        // Given empty input triggers no loop iterations and returns empty
        String result = WordWrap.from("").wrap();
        assertEquals("", result, "Expected empty string for empty input");
    }

    @Test
    @DisplayName("wrap() does not insert newline for single short word within maxWidth (tooLong false branch)")
    public void test_TC02() {
        // Single word "hello" shorter than default maxWidth=80 so tooLong false -> no newline
        String result = WordWrap.from("hello").wrap();
        assertEquals("hello", result, "Expected 'hello' with no newline as word fits within maxWidth");
    }

    @Test
    @DisplayName("wrap() breaks a long word with hyphens when breakWords=true and word exceeds maxWidth")
    public void test_TC03() {
        // Long word "longword" with maxWidth=4 forces break, insertHyphens=true (default)
        String result = WordWrap.from("longword").maxWidth(4).wrap();
        // Expect hyphens at breaks: "lo-\nngwo-\nrd"
        String expected = String.join("\n", "lo-", "ngwo-", "rd");
        assertEquals(expected, result,
                "Expected word broken with hyphens when breaking long word with default insertHyphens");
    }

    @Test
    @DisplayName("wrap() does not insert hyphens when insertHyphens=false but still breaks long word")
    public void test_TC04() {
        // Long word "longword" with maxWidth=4, breakWords=true, insertHyphens=false -> broken without hyphens
        String result = WordWrap.from("longword").maxWidth(4).insertHyphens(false).wrap();
        // Expect splits of length 3 or 4 without hyphens: "lon\ngwo\nrd"
        String expected = String.join("\n", "lon", "gwo", "rd");
        assertEquals(expected, result,
                "Expected word split without hyphens when insertHyphens is false");
    }

    @Test
    @DisplayName("wrap() does not break long word when breakWords=false even if word exceeds maxWidth")
    public void test_TC05() {
        // Long word "longword" with maxWidth=4, breakWords=false -> no breaks, returns full word
        String result = WordWrap.from("longword").maxWidth(4).breakWords(false).wrap();
        assertEquals("longword", result,
                "Expected no breaking of word when breakWords is set to false");
    }

    @Test
    @DisplayName("wrap() preserves explicit newline and trims trailing whitespace correctly (ch=='\\n' branch)")
    public void test_TC06() {
        // Input contains explicit newline so preserved, and trailing whitespace should be trimmed
        String input = "abc\ndef";
        String result = WordWrap.from(input).wrap();
        assertEquals("abc\ndef", result,
                "Expected explicit newline preserved and no additional whitespace or trims around it");
    }

    @Test
    @DisplayName("wrap() trims leading whitespace on continued lines after hyphenation (broken && line.length()==0 branch)")
    public void test_TC07() {
        // Leading spaces before word that gets broken. maxWidth=4 triggers hyphenation.
        String result = WordWrap.from("   longword").maxWidth(4).wrap();
        // Split lines and verify no line starts with a space and first fragment starts with "lo-"
        String[] lines = result.split("\n");
        assertAll("Check trimmed lines and first hyphenation",
            () -> assertTrue(lines[0].startsWith("lo-"), "First line should start with 'lo-'") ,
            () -> {
                for (String line : lines) {
                    assertFalse(line.startsWith(" "), "Line '" + line + "' should not start with leading space");
                }
            }
        );
    }
}