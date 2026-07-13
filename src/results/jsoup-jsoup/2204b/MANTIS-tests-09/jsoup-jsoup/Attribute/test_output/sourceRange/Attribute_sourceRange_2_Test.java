package org.jsoup.nodes;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Range;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Attribute_sourceRange_2_Test {

    @Test
    @DisplayName("sourceRange delegates to parent.sourceRange when parent is non-null (branch B0→B2)")
    void test_TC02() {
        // Arrange: create a stub Attributes where sourceRange(String) returns a known AttributeRange
        Attributes stubParent = new Attributes() {
            @Override
            public Range.AttributeRange sourceRange(String k) {
                // Return a fixed range to simulate delegation
                return new Range.AttributeRange(5, 6); // Correct return type
            }
        };
        // parent is non-null to satisfy the branch condition for delegation
        Attribute attr = new Attribute("attrKey", "val", stubParent);

        // Act: call sourceRange, expecting delegation to stubParent.sourceRange
        Range.AttributeRange result = attr.sourceRange();

        // Assert: verify that the returned range matches the stub's values
        assertAll(
            () -> assertEquals(5, result.getStart(), "Expected start to be 5"), // Updated method name
            () -> assertEquals(6, result.getEnd(), "Expected end to be 6") // Updated method name
        );
    }
}