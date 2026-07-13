package org.davidmoten.text.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.davidmoten.text.utils.WordWrap;
import org.davidmoten.text.utils.WordWrap.Builder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.davidmoten.guavamini.IORuntimeException;

public class WordWrap_wrap_1_Test {

    @Test
    @DisplayName("TC11: wrap(Writer) ignores carriage returns '\\r' without producing extra output")
    public void test_TC11() {
        // Input contains a carriage return which should be ignored, then newline + 'x'.
        String input = "\r\nx";
        String result = WordWrap.builder().input(input).wrapToList().get(0); // Updated method call
        assertEquals("\nx", result);
    }

    @Test
    @DisplayName("TC12: wrap(Writer) breaks long word without hyphens when insertHyphens=false")
    public void test_TC12() {
        // Word length 7, maxWidth=3, breakWords true and insertHyphens false triggers broken-word branch without hyphens
        String text = "abcdefg";
        List<String> lines = WordWrap.builder().input(text)
                .maxWidth(3)
                .breakWords(true)
                .insertHyphens(false)
                .wrap(); // Updated method call
        assertEquals(Arrays.asList("ab", "cd", "ef", "g"), lines);
    }

    @Test
    @DisplayName("TC13: wrap(Writer) uses custom stringWidth function to force every character to count as width 2 and wrap accordingly")
    public void test_TC13() {
        // Custom width func doubles count, so "abcd" total width=8, maxWidth=3 causes split in empty line branch
        String text = "abcd";
        Function<CharSequence, Number> f = s -> s.length() * 2;
        List<String> lines = WordWrap.builder().input(text)
                .maxWidth(3)
                .stringWidth(f)
                .wrap(); // Updated method call
        // Expecting break at first possible position with hyphen then remainder
        assertEquals(Arrays.asList("ab-", "cd"), lines);
    }

    @Test
    @DisplayName("TC14: wrap(LineConsumer) closes reader and wraps IOException from close into IORuntimeException")
    public void test_TC14() {
        // Custom Reader that returns EOF immediately and throws on close to hit finally block exception
        Reader faulty = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) {
                return -1; // end-of-input
            }
            @Override
            public void close() throws IOException {
                throw new IOException("close fail");
            }
        };
        Builder b = WordWrap.from(faulty, true);
        IORuntimeException ex = assertThrows(IORuntimeException.class, () -> b.wrap(new StringWriter()));
        assertTrue(ex.getCause() instanceof IOException);
    }

    @Test
    @DisplayName("TC15: fromClasspath throws NullPointerException for missing resource input stream")
    public void test_TC15() {
        // Non-existent classpath resource leads to null InputStream, causing NPE
        String res = "/nonexistent.txt";
        assertThrows(NullPointerException.class, () -> {
            // Invocation triggers InputStreamReader(null)
            WordWrap.fromClasspathUtf8(res).wrap();
        });
    }

    @Test
    @DisplayName("TC16: excludeExtraWordChars causes punctuation to break words rather than be treated as word character")
    public void test_TC16() {
        // Excluding '.' from extraWordChars makes '.' punctuation that splits words
        String text = "a.b";
        List<String> lines = WordWrap.builder().input(text)
                .maxWidth(10)
                .excludeExtraWordChars(".")
                .wrap(); // Updated method call
        // Expect 'a', '.', 'b' written in sequence when punctuation triggers word boundary
        assertEquals(Arrays.asList("a", ".", "b"), lines);
    }
}