package org.jsoup.parser;

import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Parser#setTrackPosition(boolean) and related behavior.
 */
public class Parser_setTrackPosition_2_Test {

    @Test
    @DisplayName("Fluent chaining of setTrackPosition toggles trackPosition correctly across multiple calls")
    public void test_TC05() {
        // GIVEN: a new HTML Parser with default trackPosition = false (B0 initial state)
        Parser parser = Parser.htmlParser();
        assertFalse(parser.isTrackPosition(), "Precondition: trackPosition should start false");

        // WHEN: chain toggles true->false->true to traverse B1(true)->B2->B1(false)->B2->B1(true)->B2
        parser.setTrackPosition(true)   // sets true (enter B1)
              .setTrackPosition(false)  // sets false (re-enter B1 with false)
              .setTrackPosition(true);  // sets true again (re-enter B1 with true)

        // THEN: the last invocation governs the state
        assertTrue(parser.isTrackPosition(), "After chaining, trackPosition should reflect the last call (true)");
    }

    @Test
    @DisplayName("newInstance deep copy isolation: modifying original after copy does not affect copy’s trackPosition")
    public void test_TC06() {
        // GIVEN: original parser with trackPosition explicitly enabled (B1(copy-true) path)
        Parser original = Parser.htmlParser().setTrackPosition(true);
        assertTrue(original.isTrackPosition(), "Precondition: original must have trackPosition=true before copy");

        // WHEN: deep copy via newInstance (should copy trackPosition=true), then original toggled off (B3->B4->B2)
        Parser copy = original.newInstance();
        // The deep copy should capture the trackPosition state at copy time
        original.setTrackPosition(false);

        // THEN: copy remains unaffected with trackPosition=true, original reflects new false state
        assertTrue(copy.isTrackPosition(), "Deep copy should preserve the original state at copy time (true)");
        assertFalse(original.isTrackPosition(), "Original after toggle should be false, independent from the copy");
    }
}