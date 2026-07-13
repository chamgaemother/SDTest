package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for org.jsoup.parser.Parser methods setTrackPosition and newInstance
 */
public class Parser_setTrackPosition_2_Test {

    @Test
    @DisplayName("TC05: Calling setTrackPosition(false) on a fresh parser leaves trackPosition false and returns same instance")
    public void test_TC05() {
        // GIVEN: a fresh HTML parser has trackPosition=false by default (B0)
        Parser parser = Parser.htmlParser();
        assertFalse(parser.isTrackPosition(), "Precondition: default trackPosition should be false");
        
        // WHEN: we call setTrackPosition(false) (condition false branch at B1)
        Parser result = parser.setTrackPosition(false);
        
        // THEN: it should return the same parser instance (fluent API) and keep trackPosition=false (B2)
        assertSame(parser, result, "setTrackPosition should return the same parser instance for chaining");
        assertFalse(parser.isTrackPosition(), "trackPosition should remain false when set to false on default");
    }

    @Test
    @DisplayName("TC06: newInstance() on a parser with default trackPosition=false produces a distinct parser with trackPosition=false")
    public void test_TC06() {
        // GIVEN: a fresh HTML parser with default trackPosition=false (B0)
        Parser original = Parser.htmlParser();
        assertFalse(original.isTrackPosition(), "Precondition: original trackPosition should be false");
        
        // WHEN: newInstance is called (branch B3) producing a copy; copy.trackPosition should be same as original (false) (branch B4)
        Parser copy = original.newInstance();
        
        // THEN: the new instance should not be the same object, but propagate the trackPosition state
        assertNotSame(original, copy, "newInstance should produce a distinct Parser object");
        assertFalse(copy.isTrackPosition(), "Copied parser should have trackPosition=false propagated from original");
    }
}