package org.jsoup.parser;

import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Parser#setTrackPosition(boolean) method. Tests cover enabling and disabling of position tracking.
 */
public class Parser_setTrackPosition_0_Test {

    @Test
    @DisplayName("TC01: setTrackPosition(true) sets trackPosition to true and returns the same Parser instance")
    public void test_TC01() {
        // GIVEN a fresh HTML parser with default trackPosition = false
        Parser parser = Parser.htmlParser();
        assertFalse(parser.isTrackPosition(), "Precondition: trackPosition should be false by default");

        // WHEN enabling position tracking
        Parser result = parser.setTrackPosition(true);

        // THEN the same instance is returned (branch B0→B1→B2 where trackPosition=true path)
        assertSame(parser, result, "setTrackPosition should return the same instance for chaining");
        assertTrue(parser.isTrackPosition(), "trackPosition should be true after calling setTrackPosition(true)");
    }

    @Test
    @DisplayName("TC02: setTrackPosition(false) sets trackPosition to false and returns the same Parser instance")
    public void test_TC02() {
        // GIVEN a parser with trackPosition initially set to true to force the false branch change
        Parser parser = Parser.htmlParser().setTrackPosition(true);
        assertTrue(parser.isTrackPosition(), "Precondition: trackPosition should be true after explicit enabling");

        // WHEN disabling position tracking
        Parser result = parser.setTrackPosition(false);

        // THEN the same instance is returned (branch B0→B1→B2 where trackPosition=false path)
        assertSame(parser, result, "setTrackPosition should return the same instance for chaining");
        assertFalse(parser.isTrackPosition(), "trackPosition should be false after calling setTrackPosition(false)");
    }
}