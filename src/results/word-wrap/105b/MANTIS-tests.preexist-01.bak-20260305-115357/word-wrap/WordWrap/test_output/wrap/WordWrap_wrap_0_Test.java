package org.davidmoten.text.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class WordWrap_wrap_0_Test {

    @Test
    @DisplayName("wrap() returns empty string when input is empty (no loop iterations)")
    void test_TC01_O1() {
        // GIVEN: empty input, so main loop in wordWrap sees immediate EOF and returns ""
        org.davidmoten.text.utils.WordWrap.Builder b = org.davidmoten.text.utils.WordWrap.from("");
        // WHEN
        String result = b.wrap();
        // THEN
        assertEquals("", result);
    }

    @Test
    @DisplayName("wrap() returns original short string when content length ≤ maxWidth (no wrapping)")
    void test_TC02_O1() {
        // GIVEN: "Hello World" length 11 ≤ default maxWidth 80, so no wrapping
        org.davidmoten.text.utils.WordWrap.Builder b = org.davidmoten.text.utils.WordWrap.from("Hello World");
        // WHEN
        String result = b.wrap();
        // THEN
        assertEquals("Hello World", result);
    }

    @Test
    @DisplayName("wrap() breaks a single long word with hyphens when breakWords=true and word length > maxWidth")
    void test_TC03_O1() {
        // GIVEN: single long word of length 10 > maxWidth 4, breakWords true and insertHyphens true
        org.davidmoten.text.utils.WordWrap.Builder b = org.davidmoten.text.utils.WordWrap.from("abcdefghij").maxWidth(4);
        // WHEN
        String result = b.wrap();
        // THEN: expect hyphens inserted at break points
        assertEquals("abcd-\nefgh-\nij", result);
    }

    @Test
    @DisplayName("wrap() breaks long word without hyphens when insertHyphens=false")
    void test_TC04_O1() {
        // GIVEN: single word length 8 > maxWidth 3, breakWords true, insertHyphens false
        org.davidmoten.text.utils.WordWrap.Builder b = org.davidmoten.text.utils.WordWrap.from("abcdefgh").maxWidth(3).insertHyphens(false);
        // WHEN
        String result = b.wrap();
        // THEN: breaks without hyphens
        assertEquals("abc\ndef\ngh", result);
    }

    @Test
    @DisplayName("wrap() retains existing newline and trims long lines at boundary")
    void test_TC05_O1() {
        // GIVEN: input contains '\n', maxWidth 7 so "LineOneButVeryLong" trimmed to "LineOne"
        org.davidmoten.text.utils.WordWrap.Builder b = org.davidmoten.text.utils.WordWrap.from("LineOneButVeryLong\nShort").maxWidth(7);
        // WHEN
        String result = b.wrap();
        // THEN
        assertEquals("LineOne\nShort", result);
    }

    @Test
    @DisplayName("wrap() does not break words across lines when breakWords=false")
    void test_TC06_O1() {
        // GIVEN: long word length 10 > maxWidth 4, breakWords=false so no splitting within word
        org.davidmoten.text.utils.WordWrap.Builder b = org.davidmoten.text.utils.WordWrap.from("abcdefghij").maxWidth(4).breakWords(false);
        // WHEN
        String result = b.wrap();
        // THEN
        assertEquals("abcd\nefghij", result);
    }

    @Test
    @DisplayName("wrap() uses custom stringWidth function to wrap by width of 2 units")
    void test_TC07_O1() {
        // GIVEN: custom width = length/2, so each 4 chars count as width 2, maxWidth 4→8 chars per line
        org.davidmoten.text.utils.WordWrap.Builder b = org.davidmoten.text.utils.WordWrap.from("12345678").maxWidth(4).stringWidth(s -> s.length() / 2);
        // WHEN
        String result = b.wrap();
        // THEN: original 8 chars split at 4 chars since width of 4 chars =2 units
        assertEquals("1234\n5678", result);
    }

    @Test
    @DisplayName("wrap() includes extra word characters so punctuation not split")
    void test_TC08_O1() {
        // GIVEN: '-' added to extraWordChars, so hyphenated word is treated as one word and can wrap at boundary
        org.davidmoten.text.utils.WordWrap.Builder b = org.davidmoten.text.utils.WordWrap.from("word-word").maxWidth(5).extraWordChars("-");
        // WHEN
        String result = b.wrap();
        // THEN: wrap after 'word-' since hyphen is word char
        assertEquals("word-\nword", result);
    }

    @Test
    @DisplayName("wrap() throws IORuntimeException when underlying Reader throws IOException")
    void test_TC09_O1() throws Exception {
        // GIVEN: Reader stub that throws IOException on read(), use private from(reader, close=false)
        Reader failingReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("read failure");
            }
            @Override
            public void close() throws IOException { }
        };
        Method m = org.davidmoten.text.utils.WordWrap.class.getDeclaredMethod("from", Reader.class, boolean.class);
        m.setAccessible(true);
        org.davidmoten.text.utils.WordWrap.Builder b = (org.davidmoten.text.utils.WordWrap.Builder) m.invoke(null, failingReader, false);
        // WHEN/THEN: wrap should throw IORuntimeException
        assertThrows(IORuntimeException.class, () -> b.wrap());
    }

    @Test
    @DisplayName("wrap() uses custom newLine string when provided")
    void test_TC10_O1() {
        // GIVEN: custom newLine "<NL>", default break on space when width exceeded
        org.davidmoten.text.utils.WordWrap.Builder b = org.davidmoten.text.utils.WordWrap.from("a b c d").maxWidth(3).newLine("<NL>");
        // WHEN
        String result = b.wrap();
        // THEN: lines "a b" and "c d" separated by custom delimiter
        assertEquals("a b<NL>c d", result);
    }
}