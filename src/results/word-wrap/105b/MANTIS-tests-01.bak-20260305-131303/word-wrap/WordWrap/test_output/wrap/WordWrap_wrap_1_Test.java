package org.davidmoten.text.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.davidmoten.text.utils.WordWrap;
import org.davidmoten.text.utils.IORuntimeException;

public class WordWrap_wrap_1_Test {

    @Test
    @DisplayName("Handles carriage return chars by ignoring '\r' without breaking lines")
    public void test_TC11() throws Exception {
        // Input contains '\r', ensuring carriage returns are ignored and no new lines are inserted
        Reader r = new BufferedReader(new CharSequenceReader("foo\rcd"));
        String result = WordWrap.from(r, true).wrap();
        assertEquals("foocd", result);
    }

    @Test
    @DisplayName("Skips lines that are purely whitespace (isWhitespace true path)")
    public void test_TC12() throws Exception {
        // Input is only spaces, so whitespace-only lines should be skipped entirely
        String spaces = "    ";
        String result = WordWrap.from(spaces).wrap();
        assertEquals("", result);
    }

    @Test
    @DisplayName("Treats punctuation not in extraWordChars as word boundary (isPunctuation path)")
    public void test_TC13() throws Exception {
        // Punctuation '.' not in extraWordChars should split words, with maxWidth=2 forcing break after ".a"
        Set<Character> extras = new HashSet<>();
        WordWrap.Builder b = WordWrap.from(".a b");
        b.extraWordChars(extras).maxWidth(2);
        List<String> lines = b.wrapToList();
        assertEquals(Arrays.asList(".a", "b"), lines);
    }

    @Test
    @DisplayName("wrap(File,Charset) writes to and reads from a temporary file correctly")
    public void test_TC14() throws Exception {
        // Write wrapped content to temp file and verify read-back matches direct wrap()
        File tmp = File.createTempFile("w", ".txt");
        tmp.deleteOnExit();
        String text = "alpha beta gamma";
        WordWrap.from(text).maxWidth(6).wrap(tmp, StandardCharsets.UTF_8);
        String fileContent = new String(Files.readAllBytes(tmp.toPath()), StandardCharsets.UTF_8);
        String direct = WordWrap.from(text).maxWidth(6).wrap();
        assertEquals(direct, fileContent);
    }

    @Test
    @DisplayName("Uses custom stringWidth that doubles length causing earlier breaks (tooLong on concatRightTrim)")
    public void test_TC15() throws Exception {
        // Custom width function doubles length, so "ab" width=4 > maxWidth=2 triggers break
        Function<CharSequence, Number> dbl = s -> s.length() * 2;
        List<String> lines = WordWrap.from("ab cd").stringWidth(dbl).maxWidth(2).wrapToList();
        assertEquals(Arrays.asList("ab", "cd"), lines);
    }

    @Test
    @DisplayName("Throws IORuntimeException when Reader.close() fails in finally block")
    public void test_TC16() {
        // Use a Reader whose close() throws IOException to exercise the finally block exception handling
        Reader r = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) {
                return -1; // EOF immediately
            }
            @Override
            public void close() throws IOException {
                throw new IOException("fail");
            }
        };
        // wrap() should propagate an IORuntimeException when close() fails
        assertThrows(IORuntimeException.class, () -> WordWrap.from(r, true).wrap());
    }
}