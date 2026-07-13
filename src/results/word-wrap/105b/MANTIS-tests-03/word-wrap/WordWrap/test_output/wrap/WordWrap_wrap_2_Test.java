package org.davidmoten.text.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.github.davidmoten.guavamini.io.IORuntimeException; // Corrected import statement

public class WordWrap_wrap_2_Test {

    @Test
    @DisplayName("ignore '\r' carriage return branch then emit following character on EOF")
    void test_TC11() {
        // \r should be ignored and 'A' emitted at EOF
        Reader r = new StringReader("\rA");
        WordWrap.Builder b = WordWrap.from(r);  // closeReader defaults to true
        String result = b.wrap();
        assertEquals("A", result);
    }

    @Test
    @DisplayName("punctuation branch sets previousWasPunctuation and non-letter branch retains comma")
    void test_TC12() {
        // comma is punctuation, maxWidth=3 forces wrap logic to treat comma specially
        WordWrap.Builder b = WordWrap.from("word, next").maxWidth(3);
        List<String> lines = b.wrapToList();
        // Ensure that at least one line still contains the comma
        assertTrue(lines.stream().anyMatch(l -> l.contains(",")), "Comma should be retained in output");
    }

    @Test
    @DisplayName("includeExtraWordChars makes comma a word character preventing split at comma")
    void test_TC13() {
        // comma treated as word char, so "a,b" length 2 fits maxWidth, no wrap
        WordWrap.Builder b = WordWrap.from("a,b").maxWidth(2).includeExtraWordChars(",");
        String result = b.wrap();
        assertEquals("a,b", result);
    }

    @Test
    @DisplayName("custom stringWidth function always returns constant preventing any segment exceeding maxWidth")
    void test_TC14() {
        // custom width always 1, so no segment ever exceeds maxWidth
        Function<CharSequence, Number> f = s -> 1;
        WordWrap.Builder b = WordWrap.from("longtext").stringWidth(f).maxWidth(1);
        String result = b.wrap();
        assertEquals("longtext", result);
    }

    @Test
    @DisplayName("wrap(LineConsumer) throws IORuntimeException when consumer.writeNewLine fails")
    void test_TC15() {
        // create a LineConsumer whose writeNewLine throws IOException
        LineConsumer bad = new LineConsumer() {
            @Override public void write(String s) throws IOException {}
            @Override public void write(char[] c, int off, int len) throws IOException {}
            @Override public void writeNewLine() throws IOException {
                throw new IOException("fail");
            }
        };
        Reader r = new StringReader("a\n");
        WordWrap.Builder b = WordWrap.from(r, false); // do not close reader
        IORuntimeException ex = assertThrows(IORuntimeException.class, () -> b.wrap(bad));
        assertNotNull(ex.getCause());
        assertEquals("fail", ex.getCause().getMessage());
    }

    @Test
    @DisplayName("close(reader) throws IORuntimeException when underlying Reader.close fails")
    void test_TC16() {
        // Reader that throws IOException on close()
        Reader r = new Reader() {
            @Override public int read(char[] cbuf, int off, int len) { return -1; }
            @Override public void close() throws IOException { throw new IOException("close fail"); }
        };
        WordWrap.Builder b = WordWrap.from(r, true);
        IORuntimeException ex = assertThrows(IORuntimeException.class, b::wrap);
        assertNotNull(ex.getCause());
        assertEquals("close fail", ex.getCause().getMessage());
    }

    @Test
    @DisplayName("wrap(String filename,Charset) writes wrapped output to file via overload chain")
    void test_TC17() throws IOException {
        // writing to file via filename + charset overload
        File temp = File.createTempFile("wwtest", ".txt");
        temp.deleteOnExit();
        String fname = temp.getAbsolutePath();
        WordWrap.Builder b = WordWrap.from("xyz123");
        b.wrap(fname, StandardCharsets.US_ASCII);
        byte[] data = Files.readAllBytes(temp.toPath());
        String content = new String(data, StandardCharsets.US_ASCII);
        assertEquals("xyz123", content);
    }
}