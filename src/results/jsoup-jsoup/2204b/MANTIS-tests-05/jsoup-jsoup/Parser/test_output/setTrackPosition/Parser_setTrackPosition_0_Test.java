package org.jsoup.parser;

import org.jsoup.parser.Parser;
import org.jsoup.parser.HtmlTreeBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_setTrackPosition_0_Test {

    @Test
    @DisplayName("setTrackPosition(true) sets trackPosition to true and returns the same Parser instance (branch-true)")
    public void test_TC01() {
        // GIVEN: a fresh Parser with default trackPosition=false
        Parser parser = new Parser(new HtmlTreeBuilder());
        assertFalse(parser.isTrackPosition(), "Precondition: trackPosition should start false");

        // WHEN: enabling position tracking
        Parser returned = parser.setTrackPosition(true);
        // This input true triggers branch-true in setTrackPosition

        // THEN: trackPosition is true and method-chaining returns same instance
        assertAll(
            () -> assertTrue(parser.isTrackPosition(), "trackPosition must be enabled after setTrackPosition(true)"),
            () -> assertSame(parser, returned, "setTrackPosition should return the same Parser instance for chaining")
        );
    }

    @Test
    @DisplayName("setTrackPosition(false) sets trackPosition to false and returns the same Parser instance (branch-false)")
    public void test_TC02() {
        // GIVEN: a Parser with trackPosition initially enabled
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackPosition(true);
        assertTrue(parser.isTrackPosition(), "Precondition: trackPosition should be true before disabling");

        // WHEN: disabling position tracking
        Parser returned = parser.setTrackPosition(false);
        // This input false triggers branch-false in setTrackPosition

        // THEN: trackPosition is false and method-chaining returns same instance
        assertAll(
            () -> assertFalse(parser.isTrackPosition(), "trackPosition must be disabled after setTrackPosition(false)"),
            () -> assertSame(parser, returned, "setTrackPosition should return the same Parser instance for chaining")
        );
    }
}