package org.davidmoten.text.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.davidmoten.text.utils.IORuntimeException;
public class WordWrap_wrap_1_Test {

    @Test
    @DisplayName("wrap(Writer) ignores carriage return characters (CR) in input")
    public void test_TC11() {
        // carriage return should be skipped (CR-branch) and not produce a newline or space
        StringReader r = new StringReader("a\rb");
        StringWriter w = new StringWriter();
        WordWrap.from(r, true).wrap(w);
        assertEquals("ab", w.toString());
    }

    @Test
    @DisplayName("wrap(Writer) with breakWords=false does not hyphen-break a long word and outputs it whole")
    public void test_TC12() {
        // maxWidth=2 triggers tooLong repeatedly but breakWords=false should never hyphenate, output original
        String input = "abcdef";
        StringWriter w = new StringWriter();
        WordWrap.from(input)
                .maxWidth(2)
                .breakWords(false)
                .wrap(w);
        assertEquals("abcdef", w.toString());
    }

    @Test
    @DisplayName("wrap(Writer) treats punctuation not in extraWordChars as separator and resets previousWasPunctuation flag")
    public void test_TC13() {
        // '*' is punctuation and not in extraWordChars, so it is treated as separator, then 'a' starts new word
        String text = "*a b";
        StringWriter w = new StringWriter();
        WordWrap.from(text)
                .maxWidth(10)
                .wrap(w);
        assertEquals("*a b", w.toString());
    }

    @Test
    @DisplayName("from(File,Charset) throws IORuntimeException when input file does not exist")
    public void test_TC14() {
        // nonexistent file should cause FileNotFoundException and be wrapped into IORuntimeException
        File f = new File("nonexistent12345.txt");
        assertThrows(IORuntimeException.class, () -> WordWrap.from(f, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("wrap(Writer) outputs custom newLine string CRLF when newLine is set to \r\n")
    public void test_TC15() {
        // space between 'x' and 'y' triggers line break at maxWidth=1, newLine CRLF used
        String text = "x y";
        StringWriter w = new StringWriter();
        WordWrap.from(text)
                .maxWidth(1)
                .newLine("\r\n")
                .wrap(w);
        assertEquals("x\r\ny", w.toString());
    }
}