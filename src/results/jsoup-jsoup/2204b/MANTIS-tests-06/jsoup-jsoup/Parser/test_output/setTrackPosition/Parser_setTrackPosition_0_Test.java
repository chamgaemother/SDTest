package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_setTrackPosition_0_Test {

    @Test
    @DisplayName("setTrackPosition(true) sets trackPosition to true and returns same parser instance")
    public void test_TC01() {
        // GIVEN a new Parser with default trackPosition = false
        Parser p = new Parser(new HtmlTreeBuilder());
        // WHEN enabling position tracking
        Parser result = p.setTrackPosition(true);
        // THEN the returned instance should be the same as the original (chaining) and tracking enabled
        assertSame(p, result, "setTrackPosition should return the same parser instance for chaining");
        assertTrue(result.isTrackPosition(), "After setTrackPosition(true), isTrackPosition() should return true");
    }

    @Test
    @DisplayName("setTrackPosition(false) sets trackPosition to false and returns same parser instance")
    public void test_TC02() {
        // GIVEN a Parser with trackPosition initially set to true to exercise the false branch
        Parser p = new Parser(new HtmlTreeBuilder()).setTrackPosition(true);
        assertTrue(p.isTrackPosition(), "Precondition: trackPosition should be true before disabling");
        // WHEN disabling position tracking
        Parser result = p.setTrackPosition(false);
        // THEN the returned instance should be the same as the original and tracking disabled
        assertSame(p, result, "setTrackPosition should return the same parser instance for chaining");
        assertFalse(result.isTrackPosition(), "After setTrackPosition(false), isTrackPosition() should return false");
    }
}