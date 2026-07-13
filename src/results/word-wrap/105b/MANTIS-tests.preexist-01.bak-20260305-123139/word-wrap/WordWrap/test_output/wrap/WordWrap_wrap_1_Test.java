package org.davidmoten.text.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.davidmoten.text.utils.WordWrap;
import org.davidmoten.text.utils.IORuntimeException;

/**
 * JUnit 5 tests for WordWrap.wrap scenarios TC11–TC15.
 */
public class WordWrap_wrap_1_Test {

    @Test
    @DisplayName("ignores carriage return characters (ch=='\\r') and wraps on newline only")
    void test_TC11() {
        // Use a StringReader with "a\r\nb" to exercise ignore '\r' branch and newline wrap branch
        StringReader reader = new StringReader("a\r\nb");
        StringWriter out = new StringWriter();
        // maxWidth > length so no internal wrap, only newline handling
        WordWrap.from(reader).maxWidth(10).wrap(out);
        assertEquals("a\nb", out.toString());
    }

    @Test
    @DisplayName("does not break words when breakWords=false even if word exceeds maxWidth")
    void test_TC12() {
        // Single long word "abcdef" > maxWidth=3, breakWords=false should keep it unbroken
        String input = "abcdef";
        StringWriter out = new StringWriter();
        WordWrap.from(input)
            .maxWidth(3)
            .insertHyphens(true)  // hyphens flag irrelevant when breakWords=false
            .breakWords(false)
            .wrap(out);
        assertEquals("abcdef", out.toString());
    }

    @Test
    @DisplayName("treats punctuation as non-word and triggers punctuation branch when extraWordChars excludes punctuation")
    void test_TC13() {
        // '!' is punctuation and not included in extraWordChars, so treated non-word, then 'a' appended
        String seq = "!a";
        Set<Character> extras = new HashSet<>(); // exclude all punctuation
        StringWriter out = new StringWriter();
        WordWrap.from(seq)
            .extraWordChars(extras)
            .wrap(out);
        assertEquals("!a", out.toString());
    }

    @Test
    @DisplayName("from(File,Charset) throws IORuntimeException when input file not found")
    void test_TC14() {
        // Nonexistent file should cause FileInputStream to throw FileNotFoundException wrapped in IORuntimeException
        File f = new File("does_not_exist.txt");
        assertThrows(IORuntimeException.class, () -> WordWrap.from(f, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("custom stringWidth function suppresses wrapping when tooLong always false")
    void test_TC15() {
        // Provide stringWidth that always returns 0 to force tooLong always false, so no wrapping
        String input = "longword here";
        Function<CharSequence, Number> zeroWidth = s -> 0;
        StringWriter out = new StringWriter();
        WordWrap.from(input)
            .stringWidth(zeroWidth)
            .maxWidth(1)  // with zeroWidth, both line and word never exceed maxWidth
            .wrap(out);
        assertEquals("longword here", out.toString());
    }
}