package org.jsoup.nodes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Range;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test class for Attribute.sourceRange()
 */
public class Attribute_sourceRange_1_Test {

    @Test
    @DisplayName("TC02: When parent is non-null delegates to parent.sourceRange(key)")
    public void test_TC02() {
        // Arrange: create a stub Attributes with overridden sourceRange to simulate delegation
        Attributes stubParent = new Attributes() {
            @Override
            public Range.AttributeRange sourceRange(String key) {
                // Returning known range to verify delegation
                return new Range.AttributeRange(new Range(1), new Range(2));
            }
        };
        // Create Attribute with non-null parent to hit B0→B2 branch (parent != null)
        Attribute attr = new Attribute("dataKey", "value", stubParent);

        // Act: call sourceRange, expecting delegation to stubParent
        Range.AttributeRange result = attr.sourceRange();

        // Assert: the returned range should match stub values (start=1, end=2)
        assertEquals(new Range(1), result.start(), "Expected start to be delegated from stubParent");
        assertEquals(new Range(2), result.end(), "Expected end to be delegated from stubParent");
    }
}