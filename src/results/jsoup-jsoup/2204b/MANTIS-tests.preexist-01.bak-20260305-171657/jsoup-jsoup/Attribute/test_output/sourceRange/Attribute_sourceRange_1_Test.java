package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Test class for Attribute.sourceRange method.
 */
public class Attribute_sourceRange_1_Test {

    @Test
    @DisplayName("When parent is non-null delegates to parent.sourceRange(key)")
    public void test_TC02() {
        // Arrange: create a dummy AttributeRange to be returned by the stub parent
        // Updated constructor parameters to match the correct signature
        Range.AttributeRange DUMMY = new Range.AttributeRange(new Range(5), new Range(10));
        // Stub Attributes that overrides sourceRange to return our dummy
        Attributes stubParent = new Attributes() {
            @Override
            public Range.AttributeRange sourceRange(String key) {
                // Key passed should match the attribute's key
                return DUMMY;
            }
        };
        // Create Attribute with non-null parent to follow the branch where parent != null
        Attribute attr = new Attribute("customKey", "value", stubParent);

        // Act: call sourceRange, which should delegate to stubParent.sourceRange
        Range.AttributeRange result = attr.sourceRange();

        // Assert: verify the returned instance is exactly our dummy (delegation occurred)
        Assertions.assertSame(DUMMY, result,
            "Expected sourceRange to return the AttributeRange provided by the parent stub");
    }
}