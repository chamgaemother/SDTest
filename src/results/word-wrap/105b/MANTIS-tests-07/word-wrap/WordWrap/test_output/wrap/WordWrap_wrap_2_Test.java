package org.davidmoten.text.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.davidmoten.guavamini.IORuntimeException;
import org.davidmoten.text.utils.WordWrap;
import org.davidmoten.text.utils.LineConsumer;

public class WordWrap_wrap_2_Test {

    @Test
    @DisplayName("TC16: fromClasspathUtf8 on missing resource triggers NullPointerException when reader is null")
    public void test_TC16() {
        // GIVEN no such classpath resource, reader inside Builder will wrap a stream that is null
        WordWrap.Builder b = WordWrap.from("/no-such.txt", StandardCharsets.UTF_8);
        // WHEN & THEN wrap() should attempt to read from a null InputStream and throw NPE
        assertThrows(NullPointerException.class, () -> b.wrap());
    }

    @Test
    @DisplayName("TC17: from(File,Charset) throws IORuntimeException when input file not found")
    public void test_TC17() {
        // GIVEN a non-existent file
        File f = new File("does-not-exist.txt");
        // WHEN & THEN calling from(f, UTF_8) should wrap FileNotFoundException into IORuntimeException
        assertThrows(IORuntimeException.class, () -> WordWrap.from(f, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("TC18: wrap(LineConsumer) throws IORuntimeException when consumer.writeNewLine throws IOException")
    public void test_TC18() {
        // GIVEN a Builder for input with a newline to force writeNewLine call
        WordWrap.Builder b = WordWrap.from("a\nb");
        // stub consumer that throws on writeNewLine
        LineConsumer c = new LineConsumer() {
            @Override
            public void write(char[] chars, int offset, int length) throws IOException {
                // normal write
            }
            @Override
            public void writeNewLine() throws IOException {
                throw new IOException("fail new line");
            }
            @Override
            public void write(String s) throws IOException {
                // default
            }
        };
        // WHEN & THEN wrap(c) should catch IOException and rethrow IORuntimeException
        assertThrows(IORuntimeException.class, () -> b.wrap(c));
    }

    @Test
    @DisplayName("TC19: close(Reader) rethrows IORuntimeException when reader.close() throws IOException")
    public void test_TC19() {
        // GIVEN a Reader whose close() throws IOException, and which immediately returns EOF
        Reader faulty = new StringReader("") {
            @Override
            public void close() throws IOException {
                throw new IOException("close failed");
            }
        };
        WordWrap.Builder b = WordWrap.from(faulty, true);
        Writer w = new StringWriter();
        // WHEN & THEN wrap(writer) enters finally and close(reader) should cause IORuntimeException
        assertThrows(IORuntimeException.class, () -> b.wrap(w));
    }

    @Test
    @DisplayName("TC20: static rightTrim removes trailing whitespace characters")
    public void test_TC20() {
        // GIVEN a string with trailing spaces and tabs
        String s = "abc \t  ";
        // WHEN invoking package-private rightTrim via reflection
        try {
            Method m = WordWrap.class.getDeclaredMethod("rightTrim", CharSequence.class);
            m.setAccessible(true);
            Object result = m.invoke(null, s);
            // THEN trailing whitespace is trimmed
            assertEquals("abc", result);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("TC21: static leftTrim(String) removes leading whitespace from string longer than width")
    public void test_TC21() {
        // GIVEN a string with leading spaces
        String s = "  xyz";
        // WHEN invoking private leftTrim(String) via reflection
        try {
            Method m = WordWrap.class.getDeclaredMethod("leftTrim", String.class);
            m.setAccessible(true);
            Object result = m.invoke(null, s);
            // THEN leading whitespace is trimmed
            assertEquals("xyz", result);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("TC22: writeBrokenWord else-branch splits very short word without hyphen when length≤2")
    public void test_TC22() {
        // GIVEN a short word "ab" and maxWidth=1 to force breakWords path2 (length<=2)
        WordWrap.Builder b = WordWrap.from("ab").maxWidth(1);
        // WHEN wrapToList collects lines
        List<String> lines = b.wrapToList();
        // THEN it should split into 'a' and 'b'
        assertEquals(2, lines.size());
        assertEquals("a", lines.get(0));
        assertEquals("b", lines.get(1));
    }
}