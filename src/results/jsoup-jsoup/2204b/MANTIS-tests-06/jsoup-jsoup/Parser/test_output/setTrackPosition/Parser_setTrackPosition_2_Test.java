package org.jsoup.parser;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jsoup.parser.HtmlTreeBuilder;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class Parser_setTrackPosition_2_Test {

    @Test
    @DisplayName("TC05: Fluent chaining: toggling trackPosition true→false→true returns same instance each time and final state true")
    public void test_TC05() {
        // GIVEN a fresh Parser with default trackPosition = false
        Parser p = new Parser(new HtmlTreeBuilder());

        // WHEN toggling on: enters branch B1(true) setting trackPosition from false to true
        Parser first = p.setTrackPosition(true);
        // THEN should return same instance for fluent API
        assertSame(p, first, "setTrackPosition(true) should return the same Parser instance");

        // WHEN toggling off: enters branch B1(false) setting trackPosition from true to false
        Parser second = first.setTrackPosition(false);
        // THEN should still return same instance
        assertSame(p, second, "setTrackPosition(false) should return the same Parser instance");

        // WHEN toggling on again: enters branch B1(true) setting trackPosition from false back to true
        Parser third = second.setTrackPosition(true);
        // THEN should return the same instance again
        assertSame(p, third, "setTrackPosition(true) second time should return the same Parser instance");

        // AND final state should be true after last toggle
        assertTrue(p.isTrackPosition(), "After toggling true→false→true, isTrackPosition() should be true");
    }
}