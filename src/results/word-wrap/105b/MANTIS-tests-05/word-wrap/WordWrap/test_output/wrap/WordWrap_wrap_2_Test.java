package org.davidmoten.text.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class WordWrap_wrap_2_Test {

    @Test
    @DisplayName("maxWidth(0) triggers IllegalArgumentException in Builder.maxWidth")
    public void test_TC20() {
        // GIVEN a builder from simple text; WHEN setting maxWidth to zero; THEN expect IllegalArgumentException
        WordWrap.Builder b = WordWrap.from("text");
        assertThrows(IllegalArgumentException.class, () -> {
            b.maxWidth(0);
        }); // maxWidth <=0 is invalid per contract
    }

    @Test
    @DisplayName("fromClasspath with missing resource yields NullPointerException when wrapping")
    public void test_TC21() {
        // GIVEN a builder created from a non-existing classpath resource
        WordWrap.Builder b = WordWrap.fromClasspath("/no-such.txt", StandardCharsets.UTF_8);
        StringWriter out = new StringWriter();
        // WHEN wrap is invoked, the underlying reader wraps a null InputStream, causing NPE on read()
        assertThrows(NullPointerException.class, () -> {
            b.wrap(out); // Ensure that wrap method is correctly defined in WordWrap
        });
    }

    @Test
    @DisplayName("wrap(LineConsumer) overload writes lines via direct LineConsumer API")
    public void test_TC22() throws IOException {
        // GIVEN input "A B" which contains a space so one line; tests write + writeNewLine path
        WordWrap.Builder b = WordWrap.from("A B");
        List<String> captured = new ArrayList<>();
        WordWrap.LineConsumer lc = new WordWrap.LineConsumer() {
            StringBuilder sb = new StringBuilder();
            @Override
            public void write(char[] c, int off, int len) throws IOException {
                sb.append(c, off, len); // accumulate chars
            }
            @Override
            public void writeNewLine() throws IOException {
                captured.add(sb.toString()); // on newline, capture line
                sb.setLength(0);
            }
        };
        // WHEN wrapping via consumer
        b.wrap(lc);
        // THEN exactly one line "A B" is emitted
        assertEquals(1, captured.size());
        assertEquals("A B", captured.get(0));
    }

    @Test
    @DisplayName("wrap(File, Charset) writes wrapped content to file correctly")
    public void test_TC23() throws IOException {
        // GIVEN a temp file and builder with maxWidth=3 so "one" and "two" split
        File temp = Files.createTempFile("ww", ".txt").toFile();
        temp.deleteOnExit();
        WordWrap.Builder b = WordWrap.from("one two").maxWidth(3);
        // WHEN writing to file with US_ASCII
        b.wrap(temp, StandardCharsets.US_ASCII);
        // THEN file lines are ["one","two"]
        List<String> lines = Files.readAllLines(temp.toPath(), StandardCharsets.US_ASCII);
        assertEquals(2, lines.size());
        assertEquals("one", lines.get(0));
        assertEquals("two", lines.get(1));
    }

    @Test
    @DisplayName("insertHyphens(true) but breakWords(false) does not insert hyphens or split word")
    public void test_TC24() {
        // GIVEN a single LONGWORD longer than maxWidth 4, with hyphens allowed but breakWords disabled
        WordWrap.Builder b = WordWrap.from("LONGWORD")
                .maxWidth(4)
                .insertHyphens(true)
                .breakWords(false);
        StringWriter out = new StringWriter();
        // WHEN wrapping; no hyphens or splits should occur
        b.wrap(out);
        // THEN the entire word remains intact
        assertEquals("LONGWORD", out.toString());
    }
}