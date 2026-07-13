package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.parser.Parser;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_setTrackPosition_2_Test {

    @Test
    @DisplayName("newInstance() preserves trackPosition=false when original has default setting")
    public void test_TC04() {
        // GIVEN a new HTML parser with default trackPosition (false)
        Parser original = Parser.htmlParser();
        // Inline comment: default trackPosition flag should be false as not yet set
        assertFalse(original.isTrackPosition(), "Expected default trackPosition to be false");

        // WHEN creating a copy via newInstance()
        Parser copy = original.newInstance();
        // Inline comment: copy should be a distinct instance (clone) to allow independent configuration
        assertNotSame(original, copy, "Expected newInstance() to return a new Parser instance");

        // THEN the cloned parser should preserve the trackPosition setting (false)
        // Inline comment: branch B0→B1→B2 covers the path where trackPosition is false and retained
        assertFalse(copy.isTrackPosition(), "Expected cloned parser to preserve trackPosition=false");
    }
}