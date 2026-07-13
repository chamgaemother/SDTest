package org.jsoup.nodes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jsoup.nodes.Range;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * JUnit 5 tests for Attribute.sourceRange
 */
public class Attribute_sourceRange_2_Test {

    @Test
    @DisplayName("sourceRange delegates to parent.sourceRange when parent is non-null (branch parent!=null)")
    public void test_TC02() {
        // Inline stub for Attributes: override sourceRange to return known Range
        Attributes stubParent = new Attributes() {
            @Override
            public Range.AttributeRange sourceRange() {
                // Return a stubbed range for testing delegation
                return new Range.AttributeRange(new Range.Position(7), new Range.Position(14));
            }
        };
        // Create Attribute with non-null parent to satisfy delegation branch (parent != null)
        Attribute attr = new Attribute("data-test", "value", stubParent);
        // When: call sourceRange, should delegate to stubParent.sourceRange
        Range.AttributeRange result = attr.sourceRange();
        // Then: verify that returned range matches stubbed values
        assertEquals(7, result.getStart().getPosition(), "Expected start from stub parent");
        assertEquals(14, result.getEnd().getPosition(), "Expected end from stub parent");
    }
}