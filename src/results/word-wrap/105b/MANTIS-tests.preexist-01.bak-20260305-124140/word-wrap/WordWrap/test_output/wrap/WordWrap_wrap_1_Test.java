package org.davidmoten.text.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.davidmoten.guavamini.IORuntimeException;

public class WordWrap_wrap_1_Test {

    @Test
    @DisplayName("wrap() ignores carriage return chars (ch=='\\r' branch)")
    public void test_TC11() {
        String text = "A\r\nB";
        // Changed the method call to use builder pattern
        String result = org.davidmoten.text.utils.WordWrap.builder().from(text).wrap();
        assertEquals("A\nB", result);
    }

    @Test
    @DisplayName("wrap() treats punctuation as non-word when not in extraWordChars and breaks line accordingly")
    public void test_TC12() {
        String text = "hello,world";
        String out = org.davidmoten.text.utils.WordWrap.builder().from(text)
                .maxWidth(6)
                .wrap();
        assertEquals("hello,\nworld", out);
    }

    @Test
    @DisplayName("wrap() breaks a too-long word without hyphens when insertHyphens=false and word length <=2 fallback prefix branch")
    public void test_TC13() {
        String text = "XYZ";
        String result = org.davidmoten.text.utils.WordWrap.builder().from(text)
                .maxWidth(2)
                .breakWords(true)
                .insertHyphens(false)
                .wrap();
        assertEquals("X\nYZ", result);
    }

    @Test
    @DisplayName("wrap(LineConsumer) propagates IOException as IORuntimeException")
    public void test_TC14() {
        Reader r = new StringReader("abc");
        org.davidmoten.text.utils.WordWrap.Builder b = org.davidmoten.text.utils.WordWrap.builder().from(r);
        org.davidmoten.text.utils.WordWrap.LineConsumer bad = new org.davidmoten.text.utils.WordWrap.LineConsumer() {
            @Override
            public void write(char[] c, int o, int l) throws IOException {
                throw new IOException("fail");
            }

            @Override
            public void writeNewLine() throws IOException {
                // no-op
            }
        };
        assertThrows(IORuntimeException.class, () -> {
            b.wrap(bad);
        });
    }

    @Test
    @DisplayName("wrap() respects includeExtraWordChars and does not split on included char")
    public void test_TC15() {
        String text = "a.b c";
        List<String> lines = org.davidmoten.text.utils.WordWrap.builder().from(text)
                .maxWidth(1)
                .includeExtraWordChars(".")
                .wrapToList();
        assertEquals(Arrays.asList("a.b", "c"), lines);
    }

    @Test
    @DisplayName("wrapUtf8(String) writes to file and reads back correctly (wrapUtf8(String) overload)")
    public void test_TC16() throws IOException {
        File temp = Files.createTempFile("tw", "").toFile();
        temp.deleteOnExit();
        org.davidmoten.text.utils.WordWrap.Builder b = org.davidmoten.text.utils.WordWrap.builder().from("hello world")
                .maxWidth(5);
        b.wrapUtf8(temp.getAbsolutePath());
        byte[] data = Files.readAllBytes(Paths.get(temp.getAbsolutePath()));
        String content = new String(data, StandardCharsets.UTF_8);
        assertEquals("hello\nworld", content);
    }
}