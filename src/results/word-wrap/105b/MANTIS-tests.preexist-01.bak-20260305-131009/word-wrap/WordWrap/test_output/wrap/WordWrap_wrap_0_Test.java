package org.davidmoten.text.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.davidmoten.guavamini.Preconditions;

public class WordWrap_wrap_0_Test {

    @Test
    @DisplayName("wrap() returns empty string for empty input (no loop iterations, no lines emitted)")
    public void test_TC01_O1() {
        WordWrap.Builder b = WordWrap.from("");
        String result = b.wrap();
        assertEquals("", result);
    }

    @Test
    @DisplayName("wrap() returns single line for short text under maxWidth (one iteration, no wrapping)")
    public void test_TC02_O1() {
        WordWrap.Builder b = WordWrap.from("Hello world");
        String result = b.wrap();
        assertEquals("Hello world", result);
    }

    @Test
    @DisplayName("wrap() breaks a single long word with hyphens when breakWords=true and insertHyphens=true")
    public void test_TC03_O1() {
        String text = "abcdefghijklmnopqrstuvwxyz";
        WordWrap.Builder b = WordWrap.from(text)
                                      .maxWidth(11)
                                      .breakWords(true)
                                      .insertHyphens(true);
        String result = b.wrap();
        assertEquals("abcdefghijk-\nlmnopqrstuv-\nwxyz", result);
    }

    @Test
    @DisplayName("wrap() does not insert hyphens when breakWords=true but insertHyphens=false")
    public void test_TC04_O1() {
        String text = "abcdefghijklmnopqrstuvwxyz";
        WordWrap.Builder b = WordWrap.from(text)
                                      .maxWidth(11)
                                      .breakWords(true)
                                      .insertHyphens(false);
        String result = b.wrap();
        assertEquals("abcdefghijk\nlmnopqrstuv\nwxyz", result);
    }

    @Test
    @DisplayName("wrap() retains punctuation as part of word when in extraWordChars (no break on punctuation)")
    public void test_TC05_O1() {
        String text = "Hello,world!";
        Set<Character> extras = new HashSet<>();
        extras.add(',');
        extras.add('!');
        WordWrap.Builder b = WordWrap.from(text)
                                      .extraWordChars(extras)
                                      .maxWidth(5);
        String result = b.wrap();
        assertEquals("Hello,world!", result);
    }

    @Test
    @DisplayName("wrap() splits at whitespace boundaries without breaking words")
    public void test_TC06_O1() {
        WordWrap.Builder b = WordWrap.from("Hello world")
                                      .maxWidth(5)
                                      .breakWords(false);
        String result = b.wrap();
        assertEquals("Hello\nworld", result);
    }

    @Test
    @DisplayName("wrap() uses custom stringWidth function to wrap based on width of squared length")
    public void test_TC07_O1() {
        String text = "abcdefgh";
        Function<CharSequence, Number> f = s -> s.length() / 2;
        WordWrap.Builder b = WordWrap.from(text)
                                      .maxWidth(2)
                                      .stringWidth(f);
        String result = b.wrap();
        assertEquals("abcd\n", result);
    }

    @Test
    @DisplayName("wrap() uses custom newLine delimiter")
    public void test_TC08_O1() {
        String text = "a b c";
        WordWrap.Builder b = WordWrap.from(text)
                                      .newLine("|")
                                      .maxWidth(1);
        String result = b.wrap();
        assertEquals("a|b|c", result);
    }

    @Test
    @DisplayName("wrap() throws IORuntimeException when Reader.close() throws IOException on closeReader=true")
    public void test_TC09_O1() throws Exception {
        // Stub Reader that throws on close
        Reader stub = new StringReader("dummy") {
            @Override
            public void close() throws IOException {
                super.close(); // Call super to avoid overriding access restriction
                throw new IOException("close failed");
            }
        };
        Method m = WordWrap.class.getDeclaredMethod("from", Reader.class, boolean.class);
        m.setAccessible(true);
        WordWrap.Builder b = (WordWrap.Builder) m.invoke(null, stub, true);
        assertThrows(IORuntimeException.class, () -> b.wrap());
    }

    @Test
    @DisplayName("wrap(Writer) writes wrapped text to provided Writer")
    public void test_TC10_O1() {
        StringWriter w = new StringWriter();
        WordWrap.Builder b = WordWrap.from("Hello world")
                                      .maxWidth(5);
        b.wrap(w);
        assertEquals("Hello\nworld", w.toString());
    }
}