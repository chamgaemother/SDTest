package org.davidmoten.text.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class WordWrap_wrap_2_Test {

    @Test
    @DisplayName("TC14: maxWidth(0) throws IllegalArgumentException for non‐positive width")
    public void test_TC14() {
        // GIVEN a builder on non-empty text
        WordWrap.Builder b = WordWrap.from("any");
        // WHEN & THEN maxWidth(0) should fail due to non-positive width
        assertThrows(IllegalArgumentException.class, () -> b.maxWidth(0),
                "Expected IllegalArgumentException when maxWidth is zero");
    }

    @Test
    @DisplayName("TC15: fromClasspathUtf8 of nonexistent resource leads to NullPointerException on wrap")
    public void test_TC15() {
        // GIVEN a builder from a missing classpath resource; InputStreamReader receives null stream
        String resource = "missing.txt";
        WordWrap.Builder b = WordWrap.fromClasspathUtf8(resource);
        // WHEN wrapping, read() on null underlying stream should cause NPE
        assertThrows(NullPointerException.class, () -> b.wrap(),
                "Expected NullPointerException when wrapping a missing classpath resource");
    }

    @Test
    @DisplayName("TC16: wrap(File,Charset) writes wrapped output to file and closes streams")
    public void test_TC16() throws IOException {
        // GIVEN a temporary file and input that needs wrapping at width 3
        File temp = File.createTempFile("wordwrap", ".txt");
        temp.deleteOnExit();
        String input = "one two";
        // WHEN wrapping to the file with maxWidth=3
        WordWrap.from(input).maxWidth(3).wrap(temp, StandardCharsets.UTF_8);
        // THEN file content should match expected wrapped text
        String content = new String(Files.readAllBytes(temp.toPath()), StandardCharsets.UTF_8);
        assertEquals("one\n two", content,
                "File output should wrap 'one two' at width 3 into two lines");
    }

    @Test
    @DisplayName("TC17: excludeExtraWordChars(',') treats comma as punctuation and breaks on comma")
    public void test_TC17() {
        // GIVEN text containing comma, and comma excluded from word chars so treated as punctuation
        String text = "hello,world";
        WordWrap.Builder b = WordWrap.from(text).maxWidth(6).excludeExtraWordChars(",");
        // WHEN wrapping, comma should act as break point -> break after comma
        String result = b.wrap();
        // THEN expect break at comma
        assertEquals("hello,\nworld", result,
                "Comma excluded as word char should force line break at punctuation");
    }

    @Test
    @DisplayName("TC18: broken small word (length==2) triggers writeBrokenWord else‐branch without hyphens")
    public void test_TC18() {
        // GIVEN two-character word 'ab', default breakWords=true and insertHyphens=true, maxWidth=1
        String text = "ab";
        WordWrap.Builder b = WordWrap.from(text).maxWidth(1);
        // WHEN wrapping, first char exceeds width so broken word else-branch writes one char per line
        String result = b.wrap();
        // THEN expect each character on its own line without hyphens
        assertEquals("a\nb", result,
                "Small word 'ab' broken into 'a' and 'b' without hyphens as per else-branch");
    }

    @Test
    @DisplayName("TC19: newLine(" + "\r\n" + ") causes CRLF line delimiters in output")
    public void test_TC19() {
        // GIVEN a space-delimited text and custom newLine CRLF, with maxWidth=1 to enforce break
        String text = "a b";
        WordWrap.Builder b = WordWrap.from(text).maxWidth(1).newLine("\r\n");
        // WHEN wrapping, each segment exceeds width and uses CRLF as line separator
        String result = b.wrap();
        // THEN expect CRLF separators
        assertEquals("a\r\nb", result,
                "Custom newLine CRLF should appear between wrapped lines");
    }
}