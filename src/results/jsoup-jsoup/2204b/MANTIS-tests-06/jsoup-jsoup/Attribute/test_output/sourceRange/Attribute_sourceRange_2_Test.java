package org.jsoup.nodes;

import org.jsoup.nodes.Range;
import org.jsoup.nodes.Range.AttributeRange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUnit 5 tests for {@link Attribute#sourceRange()} covering delegation and default behavior.
 */
public class Attribute_sourceRange_2_Test {

    @Test
    @DisplayName("TC02: sourceRange() delegates to parent.sourceRange when parent is non-null")
    public void test_TC02() {
        // Arrange: create a stubbed Attributes instance whose sourceRange returns a known range
        Attributes stubParent = new Attributes() {
            @Override
            public Range sourceRange() {
                // Always return a fixed range for testing delegation path
                return new Range(5, 10);
            }
        };
        // The non-null parent triggers the B0 -> B2 path (delegation)
        Attribute attr = new Attribute("data-test", "value", stubParent);

        // Act: call sourceRange, expecting delegation to stubParent.sourceRange
        Range result = attr.sourceRange();

        // Assert: the returned range equals the stubbed value
        assertEquals(new Range(5, 10), result,
            "Expected sourceRange() to return the stubbed Range from parent");
    }
}