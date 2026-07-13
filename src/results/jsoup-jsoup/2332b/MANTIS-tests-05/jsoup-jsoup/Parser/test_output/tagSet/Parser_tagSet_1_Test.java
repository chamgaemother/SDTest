package org.jsoup.parser;

import org.jsoup.parser.Parser;
import org.jsoup.parser.ParseSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_tagSet_1_Test {

    @Test
    @DisplayName("Invoke private tagSet(ParseSettings) via reflection with valid settings returns Parser instance")
    public void test_TC01() throws Exception {
        // GIVEN a fresh HTML parser and a new ParseSettings instance
        Parser parser = Parser.htmlParser();
        ParseSettings newSettings = new ParseSettings(true, true);
        // Use reflection to access the private tagSet(ParseSettings) method
        Method tagSet = Parser.class.getDeclaredMethod("tagSet", ParseSettings.class);
        tagSet.setAccessible(true);

        // WHEN invoking tagSet with a valid, non-null settings
        // This should follow path B0→B1→B2→B4: non-null arg leads to assignment and return self
        Parser returned = (Parser) tagSet.invoke(parser, newSettings);

        // THEN the returned object is the same parser instance and its settings updated
        assertSame(parser, returned, "Expected tagSet to return the same parser instance");
        assertSame(newSettings, parser.settings(), "Expected parser.settings() to reference the provided settings");
    }

    @Test
    @DisplayName("Invoke private tagSet(ParseSettings) via reflection with null argument throws NullPointerException")
    public void test_TC02() throws Exception {
        // GIVEN a fresh HTML parser
        Parser parser = Parser.htmlParser();
        // Access the private tagSet(ParseSettings) method via reflection
        Method tagSet = Parser.class.getDeclaredMethod("tagSet", ParseSettings.class);
        tagSet.setAccessible(true);

        // WHEN invoking tagSet with a null argument
        // This should follow path B0→B1→B3: null arg triggers NPE inside method
        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> {
            tagSet.invoke(parser, new Object[]{ null });
        });

        // THEN the cause of the InvocationTargetException is a NullPointerException
        assertTrue(thrown.getCause() instanceof NullPointerException,
                   "Expected cause to be NullPointerException when passing null to tagSet");
    }
}