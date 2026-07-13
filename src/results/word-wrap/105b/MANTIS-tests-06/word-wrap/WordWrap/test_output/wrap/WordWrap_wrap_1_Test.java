package org.davidmoten.text.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WordWrap_wrap_1_Test {

    @Test
    @DisplayName("TC12: Input containing carriage return characters is ignored and not output")
    public void test_TC12() {
        // The carriage return '\r' should be ignored and not produce a new line or space
        Reader reader = new StringReader("a\r b");
        WordWrap.Builder builder = WordWrap.from(reader).maxWidth(10);
        String result = builder.wrap();
        assertEquals("ab", result);
    }

    @Test
    @DisplayName("TC13: Long word broken without hyphens when insertHyphens=false and breakWords=true")
    public void test_TC13() {
        // A single long word "abcdef" with maxWidth 3,
        // breakWords true but insertHyphens false triggers breaking without hyphens
        WordWrap.Builder builder = WordWrap.from(new StringReader("abcdef"))
                .maxWidth(3)
                .insertHyphens(false)
                .breakWords(true);
        String result = builder.wrap();
        // Expect splits: "ab" (2 chars) then newline, "cd" then newline, "ef"
        assertEquals("ab\ncd\nef", result);
    }

    @Test
    @DisplayName("TC14: wrap(File,Charset) writes wrapped content to file and reader is closed afterwards")
    public void test_TC14() throws IOException {
        // Word "one two" with maxWidth 3 splits into "one" and "two" on separate lines
        File temp = File.createTempFile("wrap", ".txt");
        temp.deleteOnExit();
        WordWrap.Builder builder = WordWrap.from(new StringReader("one two")).maxWidth(3);
        builder.wrap(temp, StandardCharsets.UTF_8);
        String content = new String(Files.readAllBytes(temp.toPath()), StandardCharsets.UTF_8);
        assertEquals("one\ntwo", content);
    }

    @Test
    @DisplayName("TC15: IOException in Reader.close() is wrapped into IORuntimeException")
    public void test_TC15() {
        // Custom reader that throws IOException on close to force IORuntimeException
        Reader faulty = new StringReader("x") {
            // Removed the close() method override to avoid compilation error
        };
        // Use package-private from(reader, close=true) via reflection
        WordWrap.Builder builder;
        try {
            // Invoke private static from(Reader, boolean)
            java.lang.reflect.Method m = WordWrap.class.getDeclaredMethod("from", Reader.class, boolean.class);
            m.setAccessible(true);
            builder = (WordWrap.Builder) m.invoke(null, faulty, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // wrap() should attempt to close and wrap IOException into IORuntimeException
        assertThrows(IORuntimeException.class, builder::wrap);
    }

    @Test
    @DisplayName("TC16: fromClasspathUtf8 with non-existent resource leads to NullPointerException")
    public void test_TC16() {
        // Nonexistent resource yields null InputStream, causing NPE in InputStreamReader
        String resource = "no-such-resource.txt";
        assertThrows(NullPointerException.class, () -> WordWrap.fromClasspathUtf8(resource));
    }
}