package org.davidmoten.text.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import org.davidmoten.text.utils.WordWrap.Builder;
import org.davidmoten.text.utils.IORuntimeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class WordWrap_wrap_2_Test {

    @Test
    @DisplayName("wrap(Writer) ignores lone carriage return without producing output or error")
    public void test_TC17() throws Exception {
        // Use reflection to invoke package-private from(Reader,boolean) so that closeReader=true
        Reader r = new StringReader("\rX");
        Method m = WordWrap.class.getDeclaredMethod("from", Reader.class, boolean.class);
        m.setAccessible(true);
        Builder b = (Builder) m.invoke(null, r, true);
        StringWriter w = new StringWriter();
        // wrap should skip the carriage return and then write 'X' only
        b.wrap(w);
        assertEquals("X", w.toString());
    }

    @Test
    @DisplayName("from(File,Charset) throws IORuntimeException when source File not found")
    public void test_TC18() {
        File f = new File("nonexistent_file_123.txt");
        // calling from(f,charset) should wrap FileNotFoundException into IORuntimeException
        IORuntimeException ex = assertThrows(IORuntimeException.class, () -> {
            WordWrap.from(f, StandardCharsets.UTF_8);
        });
        // cause must be FileNotFoundException
        assertEquals(java.io.FileNotFoundException.class, ex.getCause().getClass());
    }

    @Test
    @DisplayName("wrap(Writer) breaks very short word (length=2) into prefix and remainder without hyphens")
    public void test_TC19() {
        // 'ab' with maxWidth=1 triggers tooLong at first char, line empty, breakWords true, insertHyphens true -> else branch of writeBrokenWord
        Builder b = WordWrap.from((CharSequence) "ab").maxWidth(1).insertHyphens(true);
        StringWriter w = new StringWriter();
        b.wrap(w);
        // Expect 'a' then newline then 'b'
        assertEquals("a\n" + "b", w.toString());
    }

    @Test
    @DisplayName("wrap(Writer) uses custom newLine string when writing line breaks")
    public void test_TC20() {
        // Using custom CRLF and maxWidth=1 forces break after 'a'
        Builder b = WordWrap.from((CharSequence) "a b").maxWidth(1).newLine("\r\n");
        StringWriter w = new StringWriter();
        b.wrap(w);
        // Expect 'a' + CRLF + ' b'
        assertEquals("a\r\n b", w.toString());
    }

    @Test
    @DisplayName("from(InputStream,Charset) path reads stream and wraps text correctly")
    public void test_TC21() {
        // InputStream of "hello world", wrap at width=5 splits after "hello"
        byte[] data = "hello world".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(data);
        Builder b = WordWrap.fromUtf8(in).maxWidth(5);
        StringWriter w = new StringWriter();
        b.wrap(w);
        // Should produce "hello" then newline then " world"
        assertEquals("hello\n world", w.toString());
    }
}