package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Range;
import org.jsoup.nodes.Range.AttributeRange;

import static org.junit.jupiter.api.Assertions.assertSame;
public class Attribute_sourceRange_0_Test {

    @Test
    @DisplayName("TC01: sourceRange returns UntrackedAttr when parent is null (parent == null branch)")
    public void test_TC01() {
        // Given an Attribute with no parent, so parent == null branch is taken
        Attribute attr = new Attribute("k", "v", null);

        // When calling sourceRange()
        AttributeRange result = attr.sourceRange();

        // Then expect the sentinel UntrackedAttr
        assertSame(Range.AttributeRange.UntrackedAttr, result,
                "When parent is null, sourceRange should return UntrackedAttr");
    }

    @Test
    @DisplayName("TC02: sourceRange returns parent's sourceRange when parent is non-null (parent != null branch)")
    public void test_TC02() {
        // Given a stubbed Attributes that returns a known AttributeRange for any key
        AttributeRange expected = new Range.AttributeRange(1, 2, 3); // Updated to match constructor parameters
        Attributes stubParent = new Attributes() {
            @Override
            public Range.AttributeRange sourceRange(String key) {
                // Always return our expected stubbed range
                return expected;
            }
        };
        // The Attribute is constructed with this non-null parent, so branch B0→B2 is taken
        Attribute attr = new Attribute("k", "v", stubParent);

        // When calling sourceRange()
        AttributeRange result = attr.sourceRange();

        // Then expect exactly the stubbed instance back
        assertSame(expected, result,
                "When parent is non-null, sourceRange should delegate to parent.sourceRange(key)");
    }
}