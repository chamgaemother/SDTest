package org.jsoup.parser;

import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_setTrackPosition_2_Test {

    @Test
    @DisplayName("Chaining setTrackPosition(true) twice retains trackPosition=true and returns same instance on second call")
    public void test_TC06() {
        // GIVEN: htmlParser() should start with trackPosition=false (initial B0)
        Parser parser = Parser.htmlParser();
        assertFalse(parser.isTrackPosition(), "Initial parser should not track position");
        
        // WHEN: first call to setTrackPosition(true) moves from B1 to B2 (enable tracking)
        Parser first = parser.setTrackPosition(true);
        // THEN: returns same instance and trackPosition=true
        assertSame(parser, first, "setTrackPosition should return the same parser instance on first call");
        assertTrue(parser.isTrackPosition(), "Parser should now have trackPosition=true after first call");
        
        // WHEN: second call to setTrackPosition(true) stays in B2 and then B3 (already enabled)
        Parser second = first.setTrackPosition(true);
        
        // THEN: returns same instance and trackPosition remains true
        assertSame(parser, second, "setTrackPosition should return the same parser instance on second call");
        assertTrue(parser.isTrackPosition(), "Parser should retain trackPosition=true after second call");
    }

    @Test
    @DisplayName("Chaining setTrackPosition(false) twice retains trackPosition=false and returns same instance on second call")
    public void test_TC07() {
        // GIVEN: xmlParser() should start with trackPosition=false (initial B0)
        Parser parser = Parser.xmlParser();
        assertFalse(parser.isTrackPosition(), "Initial parser should not track position");
        
        // WHEN: first call to setTrackPosition(false) stays in B3 (no change)
        Parser first = parser.setTrackPosition(false);
        // THEN: returns same instance and trackPosition remains false
        assertSame(parser, first, "setTrackPosition should return the same parser instance on first call");
        assertFalse(parser.isTrackPosition(), "Parser should still have trackPosition=false after first call");
        
        // WHEN: second call to setTrackPosition(false) again stays in B3 (still no change)
        Parser second = first.setTrackPosition(false);
        
        // THEN: returns same instance and trackPosition remains false
        assertSame(parser, second, "setTrackPosition should return the same parser instance on second call");
        assertFalse(parser.isTrackPosition(), "Parser should retain trackPosition=false after second call");
    }
}