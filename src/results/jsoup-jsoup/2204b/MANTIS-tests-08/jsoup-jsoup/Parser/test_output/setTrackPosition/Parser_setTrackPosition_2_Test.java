package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Parser#setTrackPosition and Parser#newInstance behavior regarding trackPosition value propagation.
 */
public class Parser_setTrackPosition_2_Test {

    @Test
    @DisplayName("TC05: newInstance() copies a true trackPosition value set by setTrackPosition(true)")
    public void test_TC05() {
        // GIVEN: a fresh HTML parser with default trackPosition=false
        Parser parser = Parser.htmlParser();
        // WHEN: enable position tracking on the original parser
        parser.setTrackPosition(true); // branch B1: setTrue
        // THEN: create a deep copy via newInstance which should copy trackPosition=true
        Parser copy = parser.newInstance(); // path covers copyConstructor and return

        // Verify the copy is a distinct instance (no shared state)
        assertNotSame(parser, copy, "newInstance should return a new Parser object, not the same reference");
        // Verify that trackPosition was copied as true
        assertTrue(copy.isTrackPosition(), "Copied parser should have trackPosition=true when original was set to true");
    }

    @Test
    @DisplayName("TC06: newInstance() copies a false trackPosition value after setTrackPosition(false)")
    public void test_TC06() {
        // GIVEN: a fresh HTML parser, then explicitly disable position tracking (though default=false)
        Parser parser = Parser.htmlParser().setTrackPosition(false); // branch B1: setFalse
        // WHEN: create a deep copy via newInstance which should copy trackPosition=false
        Parser copy = parser.newInstance(); // path covers copyConstructor and return

        // Verify the copy is a distinct instance (no shared state)
        assertNotSame(parser, copy, "newInstance should return a new Parser object, not the same reference");
        // Verify that trackPosition was copied as false
        assertFalse(copy.isTrackPosition(), "Copied parser should have trackPosition=false when original was set to false");
    }
}