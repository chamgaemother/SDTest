package org.davidmoten.text.utils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.davidmoten.text.utils.WordWrap;
import org.davidmoten.text.utils.WordWrap.Builder;
import org.davidmoten.text.utils.IORuntimeException;
import org.davidmoten.text.utils.LineConsumer;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
public class WordWrap_wrap_0_Test {

    @Test
    @DisplayName("TC01_O1: wrapToList produces single-line list for input shorter than maxWidth (no wrapping, loop-1)")
    public void test_TC01_O1() {
        // input length < maxWidth, so no wrapping branch taken, loop reads one character
        Builder b = WordWrap.from("hello").maxWidth(10);
        List<String> lines = b.wrapToList();
        assertAll(() -> assertEquals(1, lines.size()), 
                  () -> assertEquals("hello", lines.get(0)));
    }

    @Test
    @DisplayName("TC02_O2: wrap returns full string without newline for empty input (loop-0)")
    public void test_TC02_O2() {
        // empty input leads to zero-read loop, returns empty string
        Builder b = WordWrap.from("");
        String result = b.wrap();
        assertEquals("", result);
    }

    @Test
    @DisplayName("TC03_O3: wrap(Writer) throws IORuntimeException when Writer.write throws IOException (exception)")
    public void test_TC03_O3() {
        // stub writer always throws IOException to force IORuntimeException
        Builder b = WordWrap.from("a");
        Writer w = new Writer() {
            @Override public void write(char[] cbuf, int off, int len) throws IOException { throw new IOException("fail"); }
            @Override public void flush() throws IOException { }
            @Override public void close() throws IOException { }
        };
        assertThrows(IORuntimeException.class, () -> b.wrap(w));
    }

    @Test
    @DisplayName("TC04_O4: wrap(File,Charset) reads file and writes UTF-8 output to new file (file exists, loop-1)")
    public void test_TC04_O4() throws IOException {
        // input contains "abc def", single space, no wrap expected -> one loop
        File in = Files.createTempFile("in", ".txt").toFile();
        in.deleteOnExit();
        try (Writer out = new OutputStreamWriter(new FileOutputStream(in), StandardCharsets.UTF_8)) {
            out.write("abc def");
        }
        File out = Files.createTempFile("out", ".txt").toFile();
        out.deleteOnExit();
        WordWrap.from(in, StandardCharsets.UTF_8).wrap(out, StandardCharsets.UTF_8);
        String content = new String(Files.readAllBytes(out.toPath()), StandardCharsets.UTF_8);
        assertEquals("abc def", content);
    }

    @Test
    @DisplayName("TC05_O5: wrapUtf8(String) creates file when invoked with filename (branch-true)")
    public void test_TC05_O5() throws IOException {
        // from(CharSequence) sets closeReader=true, branch true on wrapUtf8(String)
        File out = Files.createTempFile("wraputf8", ".txt").toFile();
        out.delete(); // ensure does not exist
        String name = out.getAbsolutePath();
        Builder b = WordWrap.from("one two");
        b.wrapUtf8(name);
        File result = new File(name);
        assertTrue(result.exists());
        String content = new String(Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
        assertEquals("one two", content);
    }

    @Test
    @DisplayName("TC06_O6: wrap(String,Charset) writes using given charset and wraps long line at space (branch-true, loop-N)")
    public void test_TC06_O6() throws IOException {
        // maxWidth=5, "aa bb cc dd" splits at spaces into two lines
        File out = Files.createTempFile("wrap06", ".txt").toFile();
        out.deleteOnExit();
        String name = out.getAbsolutePath();
        Builder b = WordWrap.from("aa bb cc dd").maxWidth(5);
        b.wrap(name, StandardCharsets.US_ASCII);
        List<String> lines = Files.readAllLines(out.toPath(), StandardCharsets.US_ASCII);
        assertAll(() -> assertEquals("aa bb", lines.get(0)),
                  () -> assertEquals("cc dd", lines.get(1)));
    }

    @Test
    @DisplayName("TC07_O7: wrap(LineConsumer) processes CRLF and ignores '\\r' (branch-false, loop-N)")
    public void test_TC07_O7() {
        // CRLF input, ignore '\\r' path taken
        List<String> collected = new ArrayList<>();
        LineConsumer c = new LineConsumer() {
            private StringBuilder b = new StringBuilder();
            @Override public void write(char[] chars, int off, int len) { b.append(chars, off, len); }
            @Override public void writeNewLine() { collected.add(b.toString()); b.setLength(0); }
            @Override public void write(String s) { b.append(s); }
        };
        Builder bld = WordWrap.from("a\r\nb");
        bld.wrap(c);
        assertEquals(Arrays.asList("a","b"), collected);
    }

    @Test
    @DisplayName("TC08_O8: wrapUtf8(File) wraps and writes then closes input reader (loop-1, closeReader-true)")
    public void test_TC08_O8() throws IOException {
        // wrapUtf8(File) overload, reader should close, content "x y"
        File in = Files.createTempFile("in8", ".txt").toFile();
        in.deleteOnExit();
        try (Writer w = new OutputStreamWriter(new FileOutputStream(in), StandardCharsets.UTF_8)) {
            w.write("x y");
        }
        File out = Files.createTempFile("out8", ".txt").toFile();
        out.deleteOnExit();
        Builder b = WordWrap.from(in, StandardCharsets.UTF_8);
        b.wrapUtf8(out);
        String content = new String(Files.readAllBytes(out.toPath()), StandardCharsets.UTF_8);
        assertEquals("x y", content);
    }

    @Test
    @DisplayName("TC09_O9: wrap with includeExtraWordChars handles punctuation as word char (branch-true)")
    public void test_TC09_O9() {
        // includeExtraWordChars('.') makes '.' treated as word char, so "a.b" stays unbroken until maxWidth=3
        Builder b = WordWrap.from("a.b c").includeExtraWordChars(".").maxWidth(3);
        String result = b.wrap();
        assertEquals("a.b\nc", result);
    }

    @Test
    @DisplayName("TC10_O10: wrap disables breakWords when false so long word remains unsplit (branch-false)")
    public void test_TC10_O10() {
        // breakWords=false ensures long word "abcdef" stays intact although > maxWidth
        Builder b = WordWrap.from("abcdef").maxWidth(3).breakWords(false);
        String result = b.wrap();
        assertEquals("abcdef", result);
    }
}