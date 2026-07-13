package org.davidmoten.text.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WordWrap_wrap_0_Test {

    @Test
    @DisplayName("TC01: wrap() with empty input produces empty output (loop zero iterations)")
    public void test_TC01() {
        String text = "";
        WordWrap.Builder builder = WordWrap.from(text);
        String result = builder.wrap();
        assertEquals("", result, "Expected empty output for empty input");
    }

    @Test
    @DisplayName("TC02: wrap() with short text below maxWidth produces same text (no wrapping branch false)")
    public void test_TC02() {
        String text = "Hello World";
        WordWrap.Builder builder = WordWrap.from(text);
        String result = builder.wrap();
        assertEquals(text, result, "Expected original text when below maxWidth");
    }

    @Test
    @DisplayName("TC03: wrap() preserves explicit newline characters (branch ch=='\\n')")
    public void test_TC03() {
        String text = "a\nb\n";
        WordWrap.Builder builder = WordWrap.from(text);
        String result = builder.wrap();
        assertEquals("a\nb\n", result, "Expected newlines preserved in output");
    }

    @Test
    @DisplayName("TC04: wrap() breaks long word with hyphens when breakWords=true and insertHyphens=true")
    public void test_TC04() {
        String longWord = "AAAAAAAAAAAA";
        WordWrap.Builder builder = WordWrap.from(longWord).maxWidth(5);
        String result = builder.wrap();
        String[] lines = result.split("\\n");
        assertTrue(lines.length > 1, "Expected multiple lines after hyphenation");
        boolean foundHyphen = false;
        for (String line : lines) {
            if (line.endsWith("-")) {
                foundHyphen = true;
                break;
            }
        }
        assertTrue(foundHyphen, "Expected at least one line ending with hyphen");
    }

    @Test
    @DisplayName("TC05: wrap() does not insert hyphens when breakWords=false (branch breakWords=false)")
    public void test_TC05() {
        String longWord = "BBBBBBBB";
        WordWrap.Builder builder = WordWrap.from(longWord).maxWidth(3).breakWords(false);
        String result = builder.wrap();
        assertFalse(result.contains("-"), "Did not expect hyphens in output when breakWords is false");
    }

    @Test
    @DisplayName("TC06: wrap(LineConsumer) collects lines properly (consumer branch)")
    public void test_TC06() {
        String text = "X Y Z";
        WordWrap.Builder builder = WordWrap.from(text);
        List<String> collected = new ArrayList<>();
        builder.wrap(new LineConsumer() {
            private StringBuilder sb = new StringBuilder();
            @Override
            public void write(char[] chars, int offset, int length) throws IOException {
                sb.append(chars, offset, length);
            }
            @Override
            public void writeNewLine() throws IOException {
                collected.add(sb.toString()); sb.setLength(0);
            }
            @Override
            public void write(String s) throws IOException {
                sb.append(s);
            }
        });
        List<String> expected = builder.wrapToList();
        assertIterableEquals(expected, collected, "LineConsumer lines should match wrapToList output");
    }

    @Test
    @DisplayName("TC07: wrapToList() returns correct list of lines (loop variants)")
    public void test_TC07() {
        String text = "word1 word2";
        WordWrap.Builder builder = WordWrap.from(text).maxWidth(4);
        List<String> lines = builder.wrapToList();
        assertTrue(lines.size() > 1, "Expected more than one line for small maxWidth");
    }

    @Test
    @DisplayName("TC08: wrap(File,Charset) writes to file correctly (file I/O branch)")
    public void test_TC08() throws IOException {
        String text = "A B C";
        WordWrap.Builder builder = WordWrap.from(text);
        File temp = Files.createTempFile("ww_test", ".txt").toFile();
        temp.deleteOnExit();
        builder.wrap(temp, StandardCharsets.UTF_8);
        String fileContent = new String(Files.readAllBytes(temp.toPath()), StandardCharsets.UTF_8);
        assertEquals(builder.wrap(), fileContent, "File content should match wrap() output");
    }

    @Test
    @DisplayName("TC09: wrapUtf8(String) via filename overload writes UTF-8 file (overload:filename)")
    public void test_TC09() throws IOException {
        String text = "Test";
        WordWrap.Builder builder = WordWrap.from(text);
        File temp = Files.createTempFile("ww_test2", ".txt").toFile();
        temp.deleteOnExit();
        String name = temp.getAbsolutePath();
        builder.wrapUtf8(name);
        String fileContent = new String(Files.readAllBytes(temp.toPath()), StandardCharsets.UTF_8);
        assertEquals(builder.wrap(), fileContent, "UTF-8 file content should match wrap() output");
    }

    @Test
    @DisplayName("TC10: from(File,charset) with missing file throws IORuntimeException (exception path)")
    public void test_TC10() {
        File f = new File("nonexistent_12345.txt");
        assertThrows(IORuntimeException.class, () -> WordWrap.from(f, StandardCharsets.UTF_8));
    }
}