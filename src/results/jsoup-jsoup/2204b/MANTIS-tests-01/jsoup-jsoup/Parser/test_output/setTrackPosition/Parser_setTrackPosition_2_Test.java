package org.jsoup.parser;

import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_setTrackPosition_2_Test {

    @Test
    @DisplayName("newInstance() copies trackPosition=true state into new parser instance")
    public void test_TC05() {
        // GIVEN: an HTML parser with trackPosition explicitly enabled to exercise the branch where trackPosition=true is copied
        Parser orig = Parser.htmlParser().setTrackPosition(true);
        assertTrue(orig.isTrackPosition(), "Precondition: original parser should have trackPosition=true");

        // WHEN: creating a clone via newInstance(), which should copy the trackPosition state
        Parser clone = orig.newInstance();

        // THEN: clone must be a distinct object and preserve trackPosition=true
        assertNotSame(orig, clone, "Clone should be a different instance than original");
        assertTrue(clone.isTrackPosition(), "Cloned parser should preserve trackPosition=true");
    }

    @Test
    @DisplayName("newInstance() copies trackPosition=false state into new parser instance")
    public void test_TC06() {
        // GIVEN: an XML parser with default trackPosition (false) to exercise the branch where trackPosition=false is copied
        Parser orig = Parser.xmlParser();
        assertFalse(orig.isTrackPosition(), "Precondition: original parser should have default trackPosition=false");

        // WHEN: creating a clone via newInstance(), which should copy the trackPosition state
        Parser clone = orig.newInstance();

        // THEN: clone must be a distinct object and preserve trackPosition=false
        assertNotSame(orig, clone, "Clone should be a different instance than original");
        assertFalse(clone.isTrackPosition(), "Cloned parser should preserve trackPosition=false");
    }
}