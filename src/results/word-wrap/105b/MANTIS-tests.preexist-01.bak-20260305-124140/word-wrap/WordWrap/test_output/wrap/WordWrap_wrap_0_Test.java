package org.davidmoten.text.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class WordWrap_wrap_0_Test {

    @Test
    @DisplayName("wrap() on empty input yields empty string (loop zero iterations)")
    public void test_TC01_O1() {
        // empty input, no characters to iterate
        WordWrap.Builder b = WordWrap.from("");
        String result = b.wrap();
        assertEquals("", result, "Expected empty string for empty input");
    }

    @Test
    @DisplayName("wrap() text shorter than maxWidth remains unbroken (tooLong false)")
    public void test_TC02_O1() {
        // "hello world" length less than default maxWidth 80 => no wrap
        WordWrap.Builder b = WordWrap.from("hello world");
        String result = b.wrap();
        assertEquals("hello world", result);
    }

    @Test
    @DisplayName("wrap() splits on space when line > maxWidth (branch-true split)")
    public void test_TC03_O1() {
        // with maxWidth 6, "alpha " overflows at space => split on word boundary
        WordWrap.Builder b = WordWrap.from("alpha beta gamma").maxWidth(6);
        String result = b.wrap();
        assertEquals("alpha\nbeta gamma", result);
    }

    @Test
    @DisplayName("wrap() preserves existing newline (ch=='\\n' branch)")
    public void test_TC04_O1() {
        // newline in input should be preserved even if width <= maxWidth
        WordWrap.Builder b = WordWrap.from("one\ntwo").maxWidth(5);
        String result = b.wrap();
        assertEquals("one\ntwo", result);
    }

    @Test
    @DisplayName("wrap() breaks long word with hyphens when breakWords true")
    public void test_TC05_O1() {
        // single long word "abcdefgh" > maxWidth 3, breakWords & insertHyphens => hyphenated segments
        WordWrap.Builder b = WordWrap.from("abcdefgh").maxWidth(3)
            .breakWords(true).insertHyphens(true);
        String result = b.wrap();
        assertEquals("ab-\ncd-\nef-\ngh", result);
    }

    @Test
    @DisplayName("wrap() does not break long word when breakWords false (broken flag set)")
    public void test_TC06_O1() {
        // single long word "abcdef" > maxWidth 3, breakWords false => no break, word remains whole
        WordWrap.Builder b = WordWrap.from("abcdef").maxWidth(3)
            .breakWords(false);
        String result = b.wrap();
        assertEquals("abcdef", result);
    }

    @Test
    @DisplayName("wrap(Writer) writes wrapped text to Writer and closes reader")
    public void test_TC07_O2() throws Exception {
        // using custom Reader to verify close invoked; maxWidth 3 forces split at space
        StringReader r = new StringReader("foo bar");
        StringWriter w = new StringWriter();
        // Obtain package-private from(Reader,boolean) via reflection to set closeReader=true
        java.lang.reflect.Method m = WordWrap.class.getDeclaredMethod("from", Reader.class, boolean.class);
        m.setAccessible(true);
        WordWrap.Builder b = (WordWrap.Builder) m.invoke(null, r, true);
        b.maxWidth(3);
        b.wrap(w);
        assertEquals("foo\nbar", w.toString());
        // after wrap, reader should be closed; reading further returns -1
        assertEquals(-1, r.read());
    }

    @Test
    @DisplayName("wrapToList() returns list of lines for multi-line input")
    public void test_TC08_O3() {
        // maxWidth 1 forces each 'x','y','z' separated by spaces to own line
        WordWrap.Builder b = WordWrap.from("x y z").maxWidth(1);
        List<String> lines = b.wrapToList();
        assertEquals(Arrays.asList("x", "y", "z"), lines);
    }

    @Test
    @DisplayName("wrap(File,Charset) writes UTF-16 encoded file")
    public void test_TC09_O4() throws IOException {
        // real file IO: wrap "hi there" with maxWidth 2 produces two-line output
        File temp = Files.createTempFile("wraptest", ".txt").toFile();
        temp.deleteOnExit();
        WordWrap.Builder b = WordWrap.from("hi there").maxWidth(2);
        b.wrap(temp, StandardCharsets.UTF_16);
        // read back with UTF-16
        byte[] bytes = Files.readAllBytes(temp.toPath());
        String content = new String(bytes, StandardCharsets.UTF_16);
        assertEquals("hi\nthere", content);
    }

    @Test
    @DisplayName("wrap(String,Charset) with non-existent directory throws IORuntimeException")
    public void test_TC10_O5() {
        // non-existent directory => FileNotFoundException wrapped in IORuntimeException
        String filename = "/nonexistent_dir_xyz/out.txt";
        WordWrap.Builder b = WordWrap.from("text");
        IORuntimeException ex = assertThrows(IORuntimeException.class, () -> {
            b.wrap(filename, StandardCharsets.UTF_8);
        });
        Throwable cause = ex.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof java.io.FileNotFoundException);
    }
}