package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.Nullable;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_tagSet_2_Test {

    @Test
    @DisplayName("HTML parser recognizes known tag regardless of input case and surrounding whitespace")
    public void test_TC04() throws Exception {
        // GIVEN: an HTML parser, designed to ignore case and trim whitespace
        Parser parser = Parser.htmlParser();
        Method tagSet = Parser.class.getDeclaredMethod("tagSet", String.class);
        tagSet.setAccessible(true);
        String tag = "  DiV  "; // mixed-case with whitespace to exercise trimming (B1) and case-insensitive match (B3)

        // WHEN: invoking the private tagSet method via reflection
        boolean result = (boolean) tagSet.invoke(parser, tag);

        // THEN: should return true because 'div' is a known HTML tag and parser should ignore case/whitespace
        assertTrue(result, "Expected tagSet to recognize known tag regardless of case and whitespace");
    }

    @Test
    @DisplayName("HTML parser with case-sensitive settings rejects tag with case mismatch")
    public void test_TC05() throws Exception {
        // GIVEN: an HTML parser configured to be case-sensitive, so exact case is required
        Parser parser = Parser.htmlParser().settings(new ParseSettings(true, true));
        Method tagSet = Parser.class.getDeclaredMethod("tagSet", String.class);
        tagSet.setAccessible(true);
        String tag = "Div"; // PascalCase; should fail in case-sensitive mode (B4)

        // WHEN: invoking the private tagSet method via reflection
        boolean result = (boolean) tagSet.invoke(parser, tag);

        // THEN: should return false because case does not match exactly
        assertFalse(result, "Expected tagSet to reject tag with case mismatch under case-sensitive settings");
    }

    @Test
    @DisplayName("XML parser accepts any tag name, known or unknown")
    public void test_TC06() throws Exception {
        // GIVEN: an XML parser, which should accept any tag name unconditionally
        Parser parser = Parser.xmlParser();
        Method tagSet = Parser.class.getDeclaredMethod("tagSet", String.class);
        tagSet.setAccessible(true);
        String tag = "randomTagName"; // arbitrary name, exercising XML mode unconditional acceptance (B3)

        // WHEN: invoking the private tagSet method via reflection
        boolean result = (boolean) tagSet.invoke(parser, tag);

        // THEN: should return true for any tag under XML parsing mode
        assertTrue(result, "Expected tagSet to accept any tag under XML parsing mode");
    }
}