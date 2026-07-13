package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.parser.Parser;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Parser#setTrackPosition method.
 * Each test is independent, initializes its own Parser, and checks that setTrackPosition
 * correctly updates the trackPosition flag and returns the same instance for chaining.
 */
public class Parser_setTrackPosition_0_Test {

    @Test
    @DisplayName("Calling setTrackPosition(true) sets trackPosition flag to true and returns the same Parser instance")
    public void test_TC01() {
        // GIVEN a new HTML parser with default trackPosition = false
        Parser parser = Parser.htmlParser();
        assertFalse(parser.isTrackPosition(), "Precondition: trackPosition should be false by default");

        // WHEN enabling position tracking (branch-true path)
        Parser result = parser.setTrackPosition(true);

        // THEN the returned instance is the same as the original parser (chainable)
        assertSame(parser, result, "setTrackPosition should return the same Parser instance for chaining");
        // AND the parser's trackPosition flag is true
        assertTrue(parser.isTrackPosition(), "trackPosition must be true after setTrackPosition(true)");
    }

    @Test
    @DisplayName("Calling setTrackPosition(false) sets trackPosition flag to false and returns the same Parser instance")
    public void test_TC02() {
        // GIVEN a new HTML parser and manually enable trackPosition to true to test the false branch
        Parser parser = Parser.htmlParser().setTrackPosition(true);
        assertTrue(parser.isTrackPosition(), "Precondition: trackPosition should be true after previous setTrackPosition(true)");

        // WHEN disabling position tracking (branch-false path)
        Parser result = parser.setTrackPosition(false);

        // THEN the returned instance is the same as the original parser (chainable)
        assertSame(parser, result, "setTrackPosition should return the same Parser instance for chaining");
        // AND the parser's trackPosition flag is false
        assertFalse(parser.isTrackPosition(), "trackPosition must be false after setTrackPosition(false)");
    }
}