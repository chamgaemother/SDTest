package org.davidmoten.text.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class WordWrap_wrap_1_Test {

    @Test
    @DisplayName("ignore carriage return and break at newline when CR before LF")
    void test_TC11() {
        // Given CR before LF should be ignored, forcing break on LF only
        String text = "a\r\nb";
        String result = WordWrap.from(text).wrap();
        // Expect two lines separated by '\n'
        assertEquals("a\nb", result);
    }

    @Test
    @DisplayName("treat punctuation as non-word boundary when excluded in extraWordChars")
    void test_TC12() {
        // Given punctuation '!' excluded from extraWordChars so treated as boundary
        String text = "abcdef!gh";
        List<String> lines = WordWrap.from(text)
                .maxWidth(3)
                .excludeExtraWordChars("!")
                .wrapToList();
        // Expect split at width 3: "abc", then "def", then "!gh"
        assertEquals(3, lines.size());
        assertEquals("abc", lines.get(0));
        assertEquals("def", lines.get(1));
        assertEquals("!gh", lines.get(2));
    }

    @Test
    @DisplayName("use wrap(LineConsumer) to collect chars and newlines via custom consumer")
    void test_TC13() {
        // Given maxWidth 2 splits "hi yo" into "hi" and " yo"
        Reader reader = new StringReader("hi yo");
        WordWrap.Builder b = WordWrap.from(reader).maxWidth(2);
        StringBuilder acc = new StringBuilder();
        // Custom consumer appends '\n' as '|' marker
        b.wrap(new LineConsumer() {
            @Override
            public void write(char[] chars, int off, int len) throws IOException {
                acc.append(chars, off, len);
            }
            @Override
            public void writeNewLine() throws IOException {
                acc.append("|");
            }
            @Override
            public void write(String s) throws IOException {
                acc.append(s);
            }
        });
        // Expect "hi| yo"
        assertEquals("hi| yo", acc.toString());
    }

    @Test
    @DisplayName("final flush with broken=true and line.length()==0 trims leading whitespace on remaining word")
    void test_TC14() {
        // Given leading space and breakWords=false at maxWidth 1 causes broken=true path
        String text = " a bc";
        String result = WordWrap.from(text)
                .maxWidth(1)
                .breakWords(false)
                .wrap();
        // After final flush, leading whitespace on " bc" should be trimmed to "bc"
        assertEquals("bc", result);
    }

    @Test
    @DisplayName("wrap to file using wrapUtf8 and read file back yields correct content")
    void test_TC15() throws IOException {
        // Given temp file, wrap "x y" at width 1 -> "x\ny"
        File temp = File.createTempFile("wraptest", ".txt");
        temp.deleteOnExit();
        WordWrap.Builder b = WordWrap.from("x y").maxWidth(1);
        b.wrapUtf8(temp);
        // Read back file content in UTF-8
        byte[] data = new byte[(int) temp.length()];
        try (FileInputStream fis = new FileInputStream(temp)) {
            int read = fis.read(data);
            if (read < data.length) {
                throw new IOException("Could not read full file");
            }
        }
        String read = new String(data, StandardCharsets.UTF_8);
        assertEquals("x\ny", read);
    }

    @Test
    @DisplayName("wrap(String filename, Charset) throws IORuntimeException on unwritable path")
    void test_TC16() {
        // Given an unwritable path should throw IORuntimeException
        String badPath = "/nonexistentdir/out.txt";
        WordWrap.Builder b = WordWrap.from("test");
        assertThrows(IORuntimeException.class, () -> b.wrap(badPath, StandardCharsets.UTF_8));
    }
}