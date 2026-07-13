package org.jsoup.nodes;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Range;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class Attribute_sourceRange_2_Test {

    @Test
    @DisplayName("When parent is non-null (branch r1 != null) delegates to parent.sourceRange(key)")
    public void test_TC02() {
        // Design justification: parent is provided (non-null), so sourceRange() should delegate to parent.sourceRange(key)
        Attributes stubParent = new Attributes() {
            @Override
            public Range.AttributeRange sourceRange(String key) {
                // stub returns a known AttributeRange for verification
                return new Range.AttributeRange(5, 10);
            }
        };
        // Create Attribute with non-null parent to force delegation branch (B0→B2)
        Attribute attr = new Attribute("dataKey", "value", stubParent);

        // Invoke sourceRange, expecting delegation to stubParent.sourceRange
        Range.AttributeRange result = attr.sourceRange();

        // Assert that the returned range matches the stub's values
        assertEquals(5, result.start().value(), "Expected start=5 from delegated parent.sourceRange"); // Changed to match return type
        assertEquals(10, result.end().value(), "Expected end=10 from delegated parent.sourceRange"); // Changed to match return type
    }
}