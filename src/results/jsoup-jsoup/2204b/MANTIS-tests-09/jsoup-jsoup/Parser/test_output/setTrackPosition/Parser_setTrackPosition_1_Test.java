package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for Parser.setTrackPosition and newInstance copying behavior.
 */
public class Parser_setTrackPosition_1_Test {

    @Test
    @DisplayName("newInstance() copies trackPosition state when true")
    public void test_TC04() {
        // GIVEN: An HTML parser with trackPosition enabled (true) via setTrackPosition(true).
        // This satisfies the branch where trackPosition flag is true in the private copy constructor (B1->B2).
        Parser original = Parser.htmlParser().setTrackPosition(true);
        // Sanity: original.isTrackPosition() should be true to satisfy precondition
        assertTrue(original.isTrackPosition(), "Precondition: original should have trackPosition=true");

        // WHEN: newInstance() is called to copy the parser
        Parser copy = original.newInstance();

        // THEN: The copy retains trackPosition=true and is a distinct instance
        assertTrue(copy.isTrackPosition(), "Copied parser must retain trackPosition=true");
        assertNotSame(original, copy, "newInstance must return a distinct parser instance");
    }

    @Test
    @DisplayName("newInstance() copies trackPosition state when false")
    public void test_TC05() {
        // GIVEN: An XML parser with default trackPosition=false (no explicit set)
        // This satisfies the branch where trackPosition flag is false in the private copy constructor (B1->B3).
        Parser original = Parser.xmlParser();
        // Sanity: original.isTrackPosition() should be false to satisfy precondition
        assertFalse(original.isTrackPosition(), "Precondition: original should have trackPosition=false");

        // WHEN: newInstance() is called to copy the parser
        Parser copy = original.newInstance();

        // THEN: The copy retains trackPosition=false and is a distinct instance
        assertFalse(copy.isTrackPosition(), "Copied parser must retain trackPosition=false");
        assertNotSame(original, copy, "newInstance must return a distinct parser instance");
    }
}