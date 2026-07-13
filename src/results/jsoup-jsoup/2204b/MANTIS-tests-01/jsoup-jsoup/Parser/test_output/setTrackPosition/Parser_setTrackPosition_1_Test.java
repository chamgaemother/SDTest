package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for Parser.setTrackPosition(boolean) behavior, covering enabling and disabling position tracking.
 */
public class Parser_setTrackPosition_1_Test {

    @Test
    @DisplayName("Calling setTrackPosition(false) on a parser that already has tracking enabled disables position tracking")
    public void test_TC03() {
        // GIVEN a parser with trackPosition = true (cover path B0->B1->B2(true)->B3)
        Parser parser = Parser.htmlParser().setTrackPosition(true);
        assertTrue(parser.isTrackPosition(), "Precondition: tracking must be enabled");

        // WHEN disabling position tracking (cover branch B4 path when argument is false)
        Parser returned = parser.setTrackPosition(false);

        // THEN the same parser is returned and tracking is disabled (cover path B5)
        assertSame(parser, returned, "setTrackPosition should return the same parser instance");
        assertFalse(parser.isTrackPosition(), "After disabling, isTrackPosition() should be false");
    }

    @Test
    @DisplayName("Chaining calls to setTrackPosition flips from false to true then back to false without new instances")
    public void test_TC04() {
        // GIVEN a fresh parser with default trackPosition = false (cover B0->B1->B2(false))
        Parser parser = Parser.htmlParser();
        assertFalse(parser.isTrackPosition(), "Initial tracking should be disabled by default");

        // WHEN enabling tracking first (cover B3 for true) then disabling (cover B4 for false)
        Parser first = parser.setTrackPosition(true);
        Parser second = first.setTrackPosition(false);

        // THEN both calls return the same instance and final state is disabled (cover both B5 occurrences)
        assertSame(parser, first, "First setTrackPosition(true) should return the same parser instance");
        assertSame(parser, second, "Second setTrackPosition(false) should return the same parser instance");
        assertFalse(parser.isTrackPosition(), "After chaining, final isTrackPosition() should be false");
    }
}