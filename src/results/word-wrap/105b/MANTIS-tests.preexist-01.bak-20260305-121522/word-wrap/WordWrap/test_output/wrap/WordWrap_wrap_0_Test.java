package org.davidmoten.text.utils;

import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import org.davidmoten.text.utils.WordWrap.Builder; // Updated import statement
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.github.davidmoten.guavamini.IORuntimeException;

public class WordWrap_wrap_0_Test {

    @Test
    @DisplayName("TC01_O1: wrap() returns empty string when source text is empty (loop zero iterations)")
    public void test_TC01_O1() {
        // empty input causes no iterations through read loop
        WordWrap.Builder builder = WordWrap.from("");
        String result = builder.wrap();
        assertEquals("", result);
    }

    @Test
    @DisplayName("TC02_O1: wrap() returns original text when shorter than maxWidth (no wrapping, branch false on tooLong)")
    public void test_TC02_O1() {
        // "Hello world" length 11 <= default maxWidth 80 so tooLong always false
        WordWrap.Builder builder = WordWrap.from("Hello world");
        String result = builder.wrap();
        assertEquals("Hello world", result);
    }

    @Test
    @DisplayName("TC03_O1: wrap() splits a single long word longer than maxWidth with hyphens (breakWords true)")
    public void test_TC03_O1() {
        // long single word >10 triggers breakWords and insertHyphens => hyphen inserted
        String text = "longwordlongwordlongword";
        WordWrap.Builder builder = WordWrap.from(text).maxWidth(10);
        String result = builder.wrap();
        // first line ends with hyphen and length <=10
        String[] lines = result.split("\n");
        assertTrue(lines[0].endsWith("-"), "first line should end with hyphen");
        assertTrue(lines[0].length() <= 10, "first line length should not exceed maxWidth");
    }

    @Test
    @DisplayName("TC04_O1: wrap() does not insert hyphens when breakWords=false (insertHyphens ignored)")
    public void test_TC04_O1() {
        // long word >10 triggers breakWords=false so no hyphens
        String text = "abcdefghijKLMNOP";
        WordWrap.Builder builder = WordWrap.from(text).maxWidth(10).breakWords(false);
        String result = builder.wrap();
        assertFalse(result.contains("-"), "no hyphens should be present when breakWords=false");
        String[] lines = result.split("\n");
        // either first line empty or second line has content (word moved)
        assertTrue(lines.length >= 2, "should split into at least two lines");
        assertTrue(lines[1].length() > 0, "second line should contain remaining text");
    }

    @Test
    @DisplayName("TC05_O2: wrapToList() returns list of lines for multi-line input (newline branch true)")
    public void test_TC05_O2() {
        // input with explicit newlines triggers newline branch in wrapLoop
        WordWrap.Builder builder = WordWrap.from("a\nb\nc");
        List<String> lines = builder.wrapToList();
        assertEquals(3, lines.size());
        assertEquals("a", lines.get(0));
        assertEquals("b", lines.get(1));
        assertEquals("c", lines.get(2));
    }

    @Test
    @DisplayName("TC06_O3: wrap(Writer) rethrows IORuntimeException when Writer.write throws IOException")
    public void test_TC06_O3() {
        // Writer stub always throws IOException on write, causing IORuntimeException
        Reader reader = new StringReader("text");
        WordWrap.Builder builder = WordWrap.from(reader, true);
        Writer writer = new Writer() {
            @Override public void write(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("fail");
            }
            @Override public void flush() throws IOException { }
            @Override public void close() throws IOException { }
        };
        IORuntimeException ex = assertThrows(IORuntimeException.class, () -> builder.wrap(writer));
        assertTrue(ex.getCause() instanceof IOException);
    }

    @Test
    @DisplayName("TC07_O4: wrap(File,Charset) throws IORuntimeException when FileNotFound")
    public void test_TC07_O4() {
        // non-existent file triggers FileNotFoundException wrapped in IORuntimeException
        File file = new File("nonexistent_" + System.nanoTime() + ".txt");
        assertFalse(file.exists());
        assertThrows(IORuntimeException.class, () -> WordWrap.from(file, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("TC08_O5: wrapUtf8(String) writes wrapped output to file and can be read back")
    public void test_TC08_O5() throws IOException {
        // roundtrip: write to file then read back should match wrap()
        Path temp = Files.createTempFile("ww", ".txt");
        String filename = temp.toString();
        try {
            String text = "alpha beta gamma";
            WordWrap.Builder builder = WordWrap.from(text);
            builder.wrapUtf8(filename);
            String fileContent = new String(Files.readAllBytes(temp), StandardCharsets.UTF_8);
            assertEquals(builder.wrap(), fileContent);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    @Test
    @DisplayName("TC09_O1: wrap() uses custom newLine delimiter when provided")
    public void test_TC09_O1() {
        // custom delimiter "<NL>" replaces "\n" in wrapping
        String text = "x\ny";
        WordWrap.Builder builder = WordWrap.from(text).newLine("<NL>");
        String result = builder.wrap();
        assertEquals("x<NL>y", result);
    }

    @Test
    @DisplayName("TC10_O1: wrap() treats punctuation as word boundary when not in extraWordChars")
    public void test_TC10_O1() {
        // comma is punctuation and default extraWordChars doesn't include comma
        String text = "hello,world";
        WordWrap.Builder builder = WordWrap.from(text).maxWidth(6);
        String result = builder.wrap();
        assertTrue(result.contains("\n"), "should split at punctuation boundary");
        // ensure the first line ends with comma
        String first = result.split("\n")[0];
        assertTrue(first.endsWith(","), "punctuation should be at the end of the line");
    }
}