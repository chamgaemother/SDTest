package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * JUnit 5 tests for {@link Attribute#sourceRange()} covering delegation to parent.
 */
public class Attribute_sourceRange_1_Test {

    @Test
    @DisplayName("TC02: sourceRange delegates to parent.sourceRange when parent is non-null")
    public void test_TC02() {
        // Arrange: create a known Range.AttributeRange to return
        Range expectedRange = new Range(new Range.Position(5), new Range.Position(10)); // Updated to use Range.Position
        Range.AttributeRange expected = new Range.AttributeRange(new Range.Position(5), new Range.Position(10)); // Updated to match constructor
        // Arrange: stub parent with overridden sourceRange to return our expected instance
        Attributes stubParent = new Attributes() {
            @Override
            public Range.AttributeRange sourceRange(String key) {
                // return the stubbed expected range, simulating delegation
                return expected;
            }
        };
        // The parent is non-null, so sourceRange() should delegate to stubParent.sourceRange(key)
        Attribute attribute = new Attribute("data-key", "value", stubParent);

        // Act: call sourceRange, expecting to hit the delegation branch (parent != null)
        Range.AttributeRange result = attribute.sourceRange();

        // Assert: the returned object must be exactly the stubbed instance
        assertSame(expected, result, "Expected sourceRange() to return the stubbed parent range instance");
    }
}