package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for org.jsoup.parser.Parser#setTrackPosition(boolean)
 */
public class Parser_setTrackPosition_0_Test {

    @Test
    @DisplayName("TC01: setTrackPosition(true) sets trackPosition to true and returns the same Parser instance")
    public void test_TC01() {
        // GIVEN: a new Parser with default trackPosition = false
        Parser parser = new Parser(new HtmlTreeBuilder());
        assertFalse(parser.isTrackPosition(), "Precondition: trackPosition should start as false");
        // WHEN: enabling tracking
        Parser result = parser.setTrackPosition(true);
        // THEN: should return same instance and trackPosition become true
        assertSame(parser, result, "Expected the same instance to be returned");
        assertTrue(parser.isTrackPosition(), "After setTrackPosition(true), isTrackPosition() should be true");
        // Design justification: B1→B2 path where flag=false before call and true after call
    }

    @Test
    @DisplayName("TC02: setTrackPosition(false) sets trackPosition to false and returns the same Parser instance")
    public void test_TC02() {
        // GIVEN: a Parser with trackPosition initially set to true
        Parser parser = new Parser(new HtmlTreeBuilder()).setTrackPosition(true);
        assertTrue(parser.isTrackPosition(), "Precondition: trackPosition should be true after enabling");
        // WHEN: disabling tracking
        Parser result = parser.setTrackPosition(false);
        // THEN: should return same instance and trackPosition become false
        assertSame(parser, result, "Expected the same instance to be returned");
        assertFalse(parser.isTrackPosition(), "After setTrackPosition(false), isTrackPosition() should be false");
        // Design justification: B1→B2 path where flag=true before call and false after call
    }

    @Test
    @DisplayName("TC03: chain setTrackPosition calls and verify last call determines trackPosition")
    public void test_TC03() {
        // GIVEN: a new Parser with default trackPosition = false
        Parser parser = new Parser(new HtmlTreeBuilder());
        assertFalse(parser.isTrackPosition(), "Precondition: trackPosition should start as false");
        // WHEN: chaining calls true then false
        parser.setTrackPosition(true)  // first, set to true
              .setTrackPosition(false); // then, set back to false
        // THEN: the last call determines the state
        assertFalse(parser.isTrackPosition(), "After chaining, the last setTrackPosition(false) should prevail");
        // Design justification: covers chains of B1→B2 twice, final state false
    }
}