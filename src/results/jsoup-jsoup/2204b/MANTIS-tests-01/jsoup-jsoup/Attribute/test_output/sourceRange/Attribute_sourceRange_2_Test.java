package org.jsoup.nodes;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Range;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class Attribute_sourceRange_2_Test {

    @Test
    @DisplayName("sourceRange delegates to parent.sourceRange when parent is non-null")
    public void test_TC02() {
        // Arrange: create a known Range.AttributeRange to be returned by stubParent
        Range.Position start = new Range.Position(0); // Changed to match the expected constructor
        Range.Position end = new Range.Position(1); // Changed to match the expected constructor
        Range.AttributeRange expected = new Range.AttributeRange(start, end); // No changes needed here
        // parent is non-null so sourceRange should delegate to parent.sourceRange (path B0->B2)
        Attributes stubParent = new Attributes() {
            @Override
            public Range.AttributeRange sourceRange(String key) {
                return expected;
            }
        };
        Attribute attr = new Attribute("myKey", "myVal", stubParent);

        // Act: call sourceRange on attribute with non-null parent
        Range.AttributeRange result = attr.sourceRange();

        // Assert: the same instance returned by stubParent.sourceRange
        assertSame(expected, result);
    }
}