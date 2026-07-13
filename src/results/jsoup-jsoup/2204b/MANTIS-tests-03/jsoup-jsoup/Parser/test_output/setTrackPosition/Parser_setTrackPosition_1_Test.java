package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.jsoup.parser.Parser;
public class Parser_setTrackPosition_1_Test {

    @Test
    @DisplayName("TC03: Chaining setTrackPosition calls maintains fluent API and returns same instance each time")
    public void test_TC03() {
        // GIVEN a new HTML parser with default trackPosition = false
        Parser parser = Parser.htmlParser();
        assertFalse(parser.isTrackPosition(), "Precondition: trackPosition should start as false");

        // WHEN chaining setTrackPosition calls
        Parser p1 = parser.setTrackPosition(true);    // branch B1: set to true
        Parser p2 = p1.setTrackPosition(true);        // branch B2: set to true again
        Parser p3 = p2.setTrackPosition(false);       // branch B3: set back to false

        // THEN each call returns the same instance (fluent API)
        assertSame(parser, p1, "setTrackPosition(true) should return the same parser instance");
        assertSame(p1, p2, "setTrackPosition(true) chained should return same instance");
        assertSame(p2, p3, "setTrackPosition(false) chained should return same instance");
        // AND the final trackPosition reflects the last call (false)
        assertFalse(parser.isTrackPosition(), "After chaining, trackPosition should reflect the last setTrackPosition(false)");
    }

    @Test
    @DisplayName("TC04: Setting trackPosition before newInstance propagates state to the copied parser")
    public void test_TC04() {
        // GIVEN an original parser with trackPosition explicitly enabled
        Parser original = Parser.htmlParser().setTrackPosition(true);
        assertTrue(original.isTrackPosition(), "Precondition: original parser trackPosition should be true");

        // WHEN creating a deep copy via newInstance (branch B4→B5)
        Parser copy = original.newInstance();

        // THEN the copy is a distinct object
        assertNotSame(original, copy, "newInstance() should return a new Parser instance");
        // AND the copy inherits the trackPosition state from the original
        assertTrue(copy.isTrackPosition(), "Copied parser should have trackPosition=true as set on the original");
    }
}