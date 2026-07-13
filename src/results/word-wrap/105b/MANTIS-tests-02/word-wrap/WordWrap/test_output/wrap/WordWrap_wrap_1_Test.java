package org.davidmoten.text.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class WordWrap_wrap_1_Test {

    @Test
    @DisplayName("TC08: wrap() ignores carriage return characters (ch=='\\r' branch) and still processes subsequent newline")
    public void test_TC08() {
        // The input contains a carriage return before newline; '\r' should be ignored, '\n' triggers a line break.
        CharSequence text = "a\r\nb";
        String result = WordWrap.from(text).wrap();
        assertEquals("a\nb", result);
    }

    @Test
    @DisplayName("TC09: wrap() splits on punctuation since comma is not in extraWordChars (punctuation branch) when width exceeded")
    public void test_TC09() {
        // maxWidth=6 causes break after "hello," (6 chars) at comma boundary, comma treated as punctuation -> new line.
        String text = "hello,world";
        WordWrap.Builder b = WordWrap.from(text).maxWidth(6);
        String result = b.wrap();
        assertEquals("hello,\nworld", result);
    }

    @Test
    @DisplayName("TC10: wrap() treats comma as word character when includeExtraWordChars is used, so no split on comma")
    public void test_TC10() {
        // includeExtraWordChars(",") makes comma part of words, so width check spans entire string => no break.
        String text = "hello,world";
        WordWrap.Builder b = WordWrap.from(text)
                .maxWidth(6)
                .includeExtraWordChars(",");
        String result = b.wrap();
        assertEquals("hello,world", result);
    }

    @Test
    @DisplayName("TC11: wrapToList() collects wrapped lines into a List<String>")
    public void test_TC11() {
        // Input "abc\ndef" yields two lines at '\n' boundary.
        String text = "abc\ndef";
        List<String> lines = WordWrap.from(text).wrapToList();
        assertEquals(2, lines.size());
        assertEquals("abc", lines.get(0));
        assertEquals("def", lines.get(1));
    }

    @Test
    @DisplayName("TC12: from(File,charset) throws IORuntimeException when file not found (FileNotFoundException path)")
    public void test_TC12() {
        // Using a nonexistent file should wrap FileNotFoundException in IORuntimeException.
        File missing = new File("nonexistent.txt");
        assertThrows(IORuntimeException.class, () -> {
            WordWrap.from(missing, StandardCharsets.UTF_8);
        });
    }

    @Test
    @DisplayName("TC13: wrap() with custom stringWidth changes wrap points (tooLong uses custom Function)")
    public void test_TC13() {
        // halfWidth returns length/2; maxWidth=2 so after two chars (halfWidth("xx")=1<=2) but next makes >2 trigger break.
        String text = "xxxxx";
        Function<CharSequence, Number> halfWidth = s -> s.length() / 2;
        WordWrap.Builder b = WordWrap.from(text)
                .maxWidth(2)
                .stringWidth(halfWidth);
        String result = b.wrap();
        assertEquals("xx\nxxx", result);
    }
}