package org.jsoup.nodes;

import org.jsoup.nodes.Range.AttributeRange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Test class for Attribute#sourceRange, covering delegation behavior.
 */
public class Attribute_sourceRange_2_Test {

    @Test
    @DisplayName("TC02: When parent is non-null delegates to parent.sourceRange(key)")
    public void test_TC02() {
        // Arrange: create a dummy Range.AttributeRange and a stub Attributes that returns it
        final AttributeRange DUMMY_RANGE = new AttributeRange(new Range(1, 2)); // Changed to use Range constructor
        Attributes stubParent = new Attributes() {
            @Override
            public AttributeRange sourceRange(String key) {
                // Always return the dummy range, regardless of key
                return DUMMY_RANGE;
            }
        };
        // Provide a non-null parent to satisfy the 'parent != null' branch
        Attribute attr = new Attribute("customKey", "value", stubParent);

        // Act: invoke sourceRange, expecting delegation to stubParent.sourceRange
        AttributeRange result = attr.sourceRange();

        // Assert: ensure that the result is exactly the stub's dummy range
        assertSame(DUMMY_RANGE, result, 
            "Expected sourceRange to delegate to parent.sourceRange and return the exact instance");
    }
}