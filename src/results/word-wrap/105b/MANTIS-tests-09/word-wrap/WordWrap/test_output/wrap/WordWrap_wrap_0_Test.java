package org.davidmoten.text.utils;

import org.davidmoten.text.utils.WordWrap;
import org.davidmoten.text.utils.WordWrap.Builder;
import org.davidmoten.text.utils.LineConsumer;
import org.davidmoten.text.utils.IORuntimeException; // Corrected import
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WordWrap_wrap_0_Test {

    @Test
    @DisplayName("wrap(Writer) with single short word writes the word without line breaks")
    public void test_TC01_O1() throws Exception {
        // Single short word 'hello' below maxWidth, no wrapping occurs (branch-false)
        Builder b = WordWrap.from((CharSequence) "hello");
        StringWriter writer = new StringWriter();
        b.wrap(writer);
        assertEquals("hello", writer.toString());
    }

    @Test
    @DisplayName("wrap(Writer) with empty input does not write anything")
    public void test_TC02_O1() throws Exception {
        // Empty input produces no output, loop body never executes (loop-0)
        Builder b = WordWrap.from((CharSequence) "");
        StringWriter writer = new StringWriter();
        b.wrap(writer);
        assertEquals("", writer.toString());
    }

    @Test
    @DisplayName("wrap(Writer) with newline in input forces writeNewLine branch on '\n'")
    public void test_TC03_O1() throws Exception {
        // Input contains '\n', triggers newline branch, word appended after newline
        Builder b = WordWrap.from((CharSequence) "a\nb");
        StringWriter writer = new StringWriter();
        b.wrap(writer);
        // Expect newline preserved and second line prefixed correctly
        assertEquals("a\n b", writer.toString());
    }

    @Test
    @DisplayName("wrap(Writer) breaks long word exceeding maxWidth with hyphens")
    public void test_TC04_O1() throws Exception {
        // Word 'abcdefghij' length 10 > maxWidth 3, breakWords=true implies hyphens inserted
        Builder b = WordWrap.from((CharSequence) "abcdefghij").maxWidth(3);
        StringWriter writer = new StringWriter();
        b.wrap(writer);
        String expected = String.join("\n", Arrays.asList("ab-","cd-","ef-","gh-","ij"));
        assertEquals(expected, writer.toString());
    }

    @Test
    @DisplayName("wrap(Writer) does not break long word when breakWords=false")
    public void test_TC05_O1() throws Exception {
        // Word 'abcdef' length 6 > maxWidth 2, breakWords=false so word remains intact
        Builder b = WordWrap.from((CharSequence) "abcdef").maxWidth(2).breakWords(false);
        StringWriter writer = new StringWriter();
        b.wrap(writer);
        assertEquals("abcdef", writer.toString());
    }

    @Test
    @DisplayName("wrapToList collects wrapped lines into list")
    public void test_TC06_O2() throws Exception {
        // Input 'a b c', maxWidth=1 causes each letter on its own line
        Builder b = WordWrap.from((CharSequence) "a b c").maxWidth(1);
        List<String> lines = b.wrapToList();
        assertEquals(Arrays.asList("a", "b", "c"), lines);
    }

    @Test
    @DisplayName("wrap(LineConsumer) propagates I/O exception as IORuntimeException")
    public void test_TC07_O3() {
        // Consumer write throws IOException, should be wrapped in IORuntimeException
        Builder b = WordWrap.from((CharSequence) "x");
        LineConsumer consumer = new LineConsumer() {
            @Override
            public void write(char[] chars, int offset, int length) throws IOException {
                throw new IOException("fail");
            }
            @Override
            public void writeNewLine() throws IOException {
                // not reached
            }
            @Override
            public void write(String s) throws IOException {
                throw new IOException("fail");
            }
        };
        IORuntimeException ex = assertThrows(IORuntimeException.class, () -> b.wrap(consumer));
        assertTrue(ex.getCause() instanceof IOException);
    }

    @Test
    @DisplayName("wrap(File,Charset) writes output file and reads back content")
    public void test_TC08_O4() throws Exception {
        // Write 'hi' to temp file and confirm content
        Builder b = WordWrap.from((CharSequence) "hi");
        File f = Files.createTempFile("wraptest", ".txt").toFile();
        f.deleteOnExit();
        b.wrap(f, StandardCharsets.UTF_8);
        String content = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
        assertEquals("hi", content);
    }

    @Test
    @DisplayName("wrapUtf8(String) writes to filename with UTF-8 encoding")
    public void test_TC09_O5() throws Exception {
        // wrapUtf8 with filename writes 'ok'
        Builder b = WordWrap.from((CharSequence) "ok");
        File f = Files.createTempFile("wraptest2", ".txt").toFile();
        f.deleteOnExit();
        b.wrapUtf8(f.getAbsolutePath());
        String content = new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())), StandardCharsets.UTF_8);
        assertEquals("ok", content);
    }

    @Test
    @DisplayName("wrap() returns wrapped text as String")
    public void test_TC10_O6() {
        // wrap() no-arg returns the wrapped text directly
        Builder b = WordWrap.from((CharSequence) "bye");
        String result = b.wrap();
        assertEquals("bye", result);
    }
}