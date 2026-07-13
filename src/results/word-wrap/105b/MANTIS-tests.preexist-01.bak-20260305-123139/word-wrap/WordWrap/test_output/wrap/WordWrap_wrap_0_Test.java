package org.davidmoten.text.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class WordWrap_wrap_0_Test {

    @Test
    @DisplayName("Builder.wrap() returns original short text when under maxWidth (branch false for tooLong)")
    public void test_TC01_overload_wrap_String() {
        String input = "Hello world";
        String result = WordWrap.from(input).wrap();
        assertEquals("Hello world", result);
    }

    @Test
    @DisplayName("Builder.wrap() splits long text at whitespace when exceeding maxWidth (branch true then writeLine)")
    public void test_TC02_overload_wrap_String() {
        String input = "one two three";
        String result = WordWrap.from(input).maxWidth(7).wrap();
        assertEquals("one two\nthree", result);
    }

    @Test
    @DisplayName("Builder.wrap() breaks words with hyphens when word longer than maxWidth (insertHyphens true)")
    public void test_TC03_overload_wrap_String() {
        String input = "abcdefgh";
        String result = WordWrap.from(input).maxWidth(3).wrap();
        assertEquals("abc-\ndef-\ngh", result);
    }

    @Test
    @DisplayName("Builder.wrap() does not insert hyphens when insertHyphens(false) on long word")
    public void test_TC04_overload_wrap_String() {
        String input = "abcdef";
        String result = WordWrap.from(input).maxWidth(3).insertHyphens(false).wrap();
        assertEquals("abc\ndef", result);
    }

    @Test
    @DisplayName("Builder.wrapToList() returns list of lines for multi-line text")
    public void test_TC05_overload_wrapToList() {
        String input = "a b c d e";
        List<String> lines = WordWrap.from(input).maxWidth(3).wrapToList();
        assertEquals(Arrays.asList("a b", "c d", "e"), lines);
    }

    @Test
    @DisplayName("Builder.wrap(Writer) throws IORuntimeException when underlying writer throws IOException")
    public void test_TC06_overload_wrap_Writer() {
        Writer badWriter = new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("write failed");
            }
            @Override
            public void flush() throws IOException {
                throw new IOException("flush failed");
            }
            @Override
            public void close() throws IOException {
                // no-op
            }
        };
        WordWrap.Builder b = WordWrap.from("x");
        assertThrows(IORuntimeException.class, () -> b.wrap(badWriter));
    }

    @Test
    @DisplayName("Builder.wrap(File,Charset) throws IORuntimeException when output file cannot be created")
    public void test_TC07_overload_wrap_File_charset() {
        File badFile = new File("nonexistent_dir/subdir/output.txt");
        WordWrap.Builder b = WordWrap.from("hello");
        assertThrows(IORuntimeException.class, () -> b.wrap(badFile, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Builder.wrapUtf8(String) writes UTF-8 encoded file when called")
    public void test_TC08_overload_wrapUtf8_String() throws IOException {
        File tmp = Files.createTempFile("wordwrap", ".txt").toFile();
        tmp.deleteOnExit();
        String fname = tmp.getAbsolutePath();
        WordWrap.from("hi").wrapUtf8(fname);
        byte[] data = Files.readAllBytes(tmp.toPath());
        String content = new String(data, StandardCharsets.UTF_8);
        assertEquals("hi", content);
    }

    @Test
    @DisplayName("Builder.wrapUtf8(File) writes file with UTF-8 line breaks preserved")
    public void test_TC09_overload_wrapUtf8_File() throws IOException {
        File tmp = Files.createTempFile("wordwrap", ".txt").toFile();
        tmp.deleteOnExit();
        WordWrap.from("line1\nline2").wrapUtf8(tmp);
        byte[] data = Files.readAllBytes(tmp.toPath());
        String content = new String(data, StandardCharsets.UTF_8);
        assertEquals("line1\nline2", content);
    }

    @Test
    @DisplayName("Builder.maxWidth(0) before wrap() throws IllegalArgumentException for non-positive maxWidth")
    public void test_TC10_overload_wrap_String_illegalMaxWidth() {
        WordWrap.Builder b = WordWrap.from("x");
        assertThrows(IllegalArgumentException.class, () -> b.maxWidth(0));
    }
}