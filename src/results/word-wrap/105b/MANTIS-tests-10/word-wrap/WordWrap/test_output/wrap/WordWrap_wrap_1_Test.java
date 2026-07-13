package org.davidmoten.text.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class WordWrap_wrap_1_Test {

    @Test
    @DisplayName("TC11: Carriage return characters are ignored and Windows line endings produce correct lines")
    public void test_TC11() {
        // '\r' ignored, '\n' triggers newline branch: B0->B3->newline handling
        StringReader in = new StringReader("A\r\nB");
        WordWrap.Builder builder = WordWrap.from(in);
        List<String> lines = builder.wrapToList();
        assertEquals(Arrays.asList("A", "B"), lines, 
            "Expected two lines [\"A\",\"B\"] for input with CR+LF");
    }

    @Test
    @DisplayName("TC12: Punctuation suppresses normal word accumulation and attaches to line before next word")
    public void test_TC12() {
        // '?' is punctuation, previousWasPunctuation starts false then true, forcing else branch->appendWordToLine
        StringReader in = new StringReader("?hello");
        WordWrap.Builder builder = WordWrap.from(in);
        String result = builder.wrap();
        assertEquals("?hello", result,
            "Expected punctuation at start to be preserved and attached to the word");
    }

    @Test
    @DisplayName("TC13: Breaking words without hyphens splits on single char and no hyphen inserted")
    public void test_TC13() {
        // 'abcdef' tooLong triggers word-breaking path, breakWords=true and insertHyphens=false -> no-hyphen branch
        StringReader in = new StringReader("abcdef");
        WordWrap.Builder builder = WordWrap.from(in)
                                         .maxWidth(3)
                                         .insertHyphens(false);
        List<String> lines = builder.wrapToList();
        assertEquals(Arrays.asList("ab", "cd", "ef"), lines,
            "Expected split into chunks of 2 characters without hyphens for maxWidth=3");
    }

    @Test
    @DisplayName("TC14: Whitespace-only input produces no output or trailing blank lines")
    public void test_TC14() {
        // input only spaces, isWhitespace(word) true at EOF branch -> no output
        StringReader in = new StringReader("   ");
        WordWrap.Builder builder = WordWrap.from(in);
        String result = builder.wrap();
        assertEquals("", result,
            "Expected empty string for whitespace-only input");
    }

    @Test
    @DisplayName("TC15: Custom stringWidth causing right-trim on overflow at explicit newline")
    public void test_TC15() throws IOException {
        // custom width always 3 -> tooLong on "abc   ", newline branch -> rightTrim drops spaces
        StringReader in = new StringReader("abc   \n");
        Function<CharSequence, Number> f = s -> 3; // force tooLong condition
        WordWrap.Builder builder = WordWrap.from(in)
                                         .stringWidth(f)
                                         .maxWidth(2);
        StringWriter out = new StringWriter();
        builder.wrap(out);
        assertEquals("abc\n", out.toString(),
            "Expected trailing spaces trimmed before newline when custom width forces overflow");
    }
}