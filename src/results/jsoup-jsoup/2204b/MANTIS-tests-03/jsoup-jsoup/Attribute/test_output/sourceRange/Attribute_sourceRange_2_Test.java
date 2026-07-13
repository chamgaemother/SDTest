package org.jsoup.nodes;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test class for Attribute.sourceRange()
 */
public class Attribute_sourceRange_2_Test {

    @Test
    @DisplayName("TC02: sourceRange() delegates to parent.sourceRange(key) when parent is non-null")
    public void test_TC02() {
        // Arrange: create a fake AttributeRange and a stubbed Attributes parent
        Range.Position start = new Range.Position(1, 0); // Ensure correct parameter types
        Range.Position end = new Range.Position(2, 0);   // Ensure correct parameter types
        Range.AttributeRange fakeRange = new Range.AttributeRange(start, end);
        // parent is non-null, so intended to delegate to stubParent.sourceRange(key)
        Attributes stubParent = new Attributes() {
            @Override
            public Range.AttributeRange sourceRange(String key) {
                // return our fake range to verify delegation
                return fakeRange;
            }
        };
        Attribute attr = new Attribute("testKey", "testVal", stubParent);

        // Act: call sourceRange()
        Range.AttributeRange result = attr.sourceRange(); // Cast removed if not necessary

        // Assert: should be the exact instance returned by stubParent.sourceRange(key)
        assertSame(fakeRange, result, "When parent is non-null, sourceRange() should delegate to parent.sourceRange(key)");
    }
}