package org.davidmoten.text.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class WordWrap_wrap_0_Test {

    @Test
    @DisplayName("TC01: wrap(Writer) with empty input triggers zero-loop and writes nothing")
    public void test_TC01() {
        // empty input so loop never executes, no output should be written
        WordWrap.Builder builder = WordWrap.from(new StringReader(""), true);
        StringWriter out = new StringWriter();
        builder.wrap(out);
        assertEquals("", out.toString());
    }

    @Test
    @DisplayName("TC02: wrap(Writer) with single short word uses branch isWordCharacter=true and no wrapping")
    public void test_TC02() {
        // single word shorter than default maxWidth so isWordCharacter path, no wrap
        WordWrap.Builder builder = WordWrap.from(new StringReader("Hello"), true);
        StringWriter out = new StringWriter();
        builder.wrap(out);
        assertEquals("Hello", out.toString());
    }

    @Test
    @DisplayName("TC03: wrap(Writer) with newline character flushes line branch ch=='\n'")
    public void test_TC03() {
        // input includes '\n', forcing flush of line and then writing remainder
        WordWrap.Builder builder = WordWrap.from(new StringReader("Hi\nThere"), true);
        StringWriter out = new StringWriter();
        builder.wrap(out);
        assertEquals("Hi\nThere", out.toString());
    }

    @Test
    @DisplayName("TC04: wrap(Writer) breaks long word with hyphens when breakWords=true and insertHyphens=true")
    public void test_TC04() {
        // long word length 11, maxWidth 5 triggers tooLong and broken with hyphens
        WordWrap.Builder builder = WordWrap.from(new StringReader("abcdefghijk"), true)
                .maxWidth(5);
        StringWriter out = new StringWriter();
        builder.wrap(out);
        // expect break at 4 chars + '-' then newline, then remaining 5 chars
        assertEquals("abcd-\nejijk", out.toString());
    }

    @Test
    @DisplayName("TC05: wrap(Writer) with breakWords=false leaves word unbroken and flags broken=true")
    public void test_TC05() {
        // long word and breakWords=false should not insert hyphens, entire word remains
        WordWrap.Builder builder = WordWrap.from(new StringReader("abcdefgh"), true)
                .maxWidth(5)
                .breakWords(false);
        StringWriter out = new StringWriter();
        builder.wrap(out);
        assertEquals("abcdefgh", out.toString());
    }

    @Test
    @DisplayName("TC06: wrapToList returns list of lines for multi-line input including trailing word")
    public void test_TC06() {
        // words separated by spaces, maxWidth=4 forces wrap between words
        WordWrap.Builder builder = WordWrap.from(new StringReader("one two three"), true)
                .maxWidth(4);
        List<String> lines = builder.wrapToList();
        assertEquals(Arrays.asList("one", "two", "three"), lines);
    }

    @Test
    @DisplayName("TC07: wrap() returns concatenated String equivalent to wrap(Writer)")
    public void test_TC07() {
        // wrap() should produce same output as wrap(writer)
        WordWrap.Builder builder = WordWrap.from(new StringReader("alpha beta"), true);
        String result = builder.wrap();
        assertEquals("alpha beta", result);
    }

    @Test
    @DisplayName("TC08: wrap(File, Charset) throws IORuntimeException when output file cannot be written")
    public void test_TC08() {
        // use a directory as target to cause write failure
        WordWrap.Builder builder = WordWrap.from(new StringReader("data"), true);
        File dir = new File(System.getProperty("java.io.tmpdir"), "unwritableDir");
        dir.mkdirs();
        // make directory unwritable
        dir.setWritable(false);
        File target = new File(dir, "file.txt");
        try {
            assertThrows(IORuntimeException.class, () -> builder.wrap(target, StandardCharsets.UTF_8));
        } finally {
            // cleanup
            dir.setWritable(true);
            dir.delete();
        }
    }

    @Test
    @DisplayName("TC09: wrapUtf8(String) writes UTF-8 file via wrap(File,Charset)")
    public void test_TC09() throws IOException {
        // cyrillic word to verify UTF-8 encoding
        WordWrap.Builder builder = WordWrap.from(new StringReader("тест"), true);
        File tmp = File.createTempFile("wrapTest", ".txt");
        tmp.deleteOnExit();
        builder.wrapUtf8(tmp.getAbsolutePath());
        byte[] bytes = Files.readAllBytes(Paths.get(tmp.getAbsolutePath()));
        String content = new String(bytes, StandardCharsets.UTF_8);
        assertEquals("тест", content);
    }

    @Test
    @DisplayName("TC10: wrap(LineConsumer) with consumer writes each new line via writeNewLine branch")
    public void test_TC10() {
        // input contains newline, consumer should record two lines
        WordWrap.Builder builder = WordWrap.from(new StringReader("a b\na"), true);
        TestConsumer consumer = new TestConsumer();
        builder.wrap(consumer);
        assertEquals(Arrays.asList("a b", "a"), consumer.lines);
    }

    // simple LineConsumer implementation for tests
    private static class TestConsumer implements LineConsumer {
        private final java.util.List<String> lines = new java.util.ArrayList<>();
        private final StringBuilder b = new StringBuilder();

        @Override
        public void write(char[] chars, int offset, int length) {
            b.append(chars, offset, length);
        }

        @Override
        public void writeNewLine() {
            lines.add(b.toString());
            b.setLength(0);
        }

        @Override
        public void write(String s) {
            b.append(s);
        }
    }
}