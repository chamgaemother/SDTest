package org.davidmoten.text.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.davidmoten.text.utils.WordWrap; // Ensure this import is present
import org.davidmoten.text.utils.IORuntimeException; // Added import for IORuntimeException

public class WordWrap_wrap_2_Test {

    @Test
    @DisplayName("wrap(String filename, Charset) writes wrapped output to named file and reads it back via wrapUtf8(String)")
    public void test_TC17() throws IOException {
        // GIVEN: text that upon wrapping will produce predictable output
        String text = "alpha beta gamma";
        String tmpFilename = Files.createTempFile("ww", ".txt").toString();
        // WHEN: write wrapped output to file via overload
        WordWrap.from(text).maxWidth(6).wrap(tmpFilename, StandardCharsets.UTF_8);
        // read back the file and also get direct wrap
        String fileContent = new String(Files.readAllBytes(new File(tmpFilename).toPath()), StandardCharsets.UTF_8);
        String direct = WordWrap.from(text).maxWidth(6).wrap();
        // THEN: file content equals direct wrap output
        assertEquals(direct, fileContent);
        // cleanup
        new File(tmpFilename).delete();
    }

    @Test
    @DisplayName("from(File,Charset) throws IORuntimeException when input file not found")
    public void test_TC18() {
        // GIVEN: non-existent file reference
        File f = new File("nonexistent_file_hopefully_does_not_exist.txt");
        // WHEN/THEN: from(File,Charset) should throw IORuntimeException
        assertThrows(IORuntimeException.class, () -> WordWrap.from(f, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Long word broken without hyphens when insertHyphens=false and breakWords=true (writeBrokenWord else-branch)")
    public void test_TC19() {
        // GIVEN: single long word to force breakWords path without hyphens
        String w = "abcdef"; // length 6 > maxWidth
        // WHEN: set maxWidth small, enable breakWords and disable hyphens
        String result = WordWrap.from(w)
                .maxWidth(2)                // force tooLong on first word fragment
                .breakWords(true)           // allow breaking words
                .insertHyphens(false)       // disable hyphens so else branch in writeBrokenWord
                .wrap();
        // THEN: result starts with at least two chars and contains a newline, and no hyphens
        assertEquals(true, result.startsWith("ab"), "Expected result to start with broken fragment 'ab'");
        assertEquals(true, result.contains("\n"), "Expected a line break in broken word output");
        assertFalse(result.contains("-"), "Expected no hyphens when insertHyphens is false");
    }

    @Test
    @DisplayName("Trailing spaces before newline are trimmed by rightTrim path")
    public void test_TC20() {
        // GIVEN: text with trailing spaces before explicit newline
        String text = "foo    \n";
        // WHEN: wrap with default settings should trim trailing spaces on newline
        String result = WordWrap.from(text).wrap();
        // THEN: trailing spaces removed, only 'foo' and newline remain
        assertEquals("foo\n", result);
    }

    @Test
    @DisplayName("wrap(LineConsumer) uses custom consumer to collect char arrays and newLine events")
    public void test_TC21() {
        // GIVEN: custom LineConsumer collecting writes and new lines
        List<String> parts = new ArrayList<>();
        LineConsumer consumer = new LineConsumer() {
            @Override
            public void write(char[] chars, int off, int len) {
                parts.add(new String(chars, off, len));
            }

            @Override
            public void writeNewLine() {
                parts.add("<NL>");
            }

            @Override
            public void write(String s) throws IOException {
                // Not used in this test
                parts.add(s);
            }
        };
        // WHEN: wrap text "a bc" with maxWidth 1 causes each char on its own line via consumer
        WordWrap.from("a bc").maxWidth(1).wrap(consumer);
        // THEN: parts list should reflect individual chars and new line tokens
        List<String> expected = Arrays.asList("a", "<NL>", "b", "<NL>", "c");
        assertEquals(expected, parts);
    }
}