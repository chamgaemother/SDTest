package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for {@link Attribute#sourceRange()} method.
 */
public class Attribute_sourceRange_1_Test {

    @Test
    @DisplayName("sourceRange() delegates to parent.sourceRange when parent is non-null")
    public void test_TC02() {
        // Arrange a stub Attributes where sourceRange(String) returns a known custom range
        Attributes stubParent = new Attributes() {
            @Override
            public Range.AttributeRange sourceRange(String key) {
                // return a custom range to verify delegation
                return new Range.AttributeRange(5, 10); // Ensure this returns a valid Range.AttributeRange object
            }
        };
        // Create an Attribute with non-null parent to drive the B0→B2 branch
        Attribute attr = new Attribute("data-test", "value", stubParent);

        // Act: call sourceRange, expecting delegation to stubParent.sourceRange
        Range.AttributeRange result = attr.sourceRange();

        // Assert: the returned range matches the custom stub range
        assertEquals(new Range.AttributeRange(5, 10), result,
                "Expected sourceRange() to delegate to parent and return the stubbed range"); // Ensure proper comparison
    }
}