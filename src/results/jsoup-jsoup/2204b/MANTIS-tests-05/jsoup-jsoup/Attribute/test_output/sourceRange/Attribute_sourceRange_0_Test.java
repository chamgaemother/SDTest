package org.jsoup.nodes;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Range;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class Attribute_sourceRange_0_Test {

    @Test
    @DisplayName("sourceRange returns UntrackedAttr when parent is null (parent==null branch)")
    public void test_TC01() {
        // Given an Attribute with no parent, parent is null so B0→B1 path is taken
        Attribute attr = new Attribute("key", "value", null);
        // When calling sourceRange
        Range.AttributeRange result = attr.sourceRange();
        // Then expect the UntrackedAttr constant per spec
        assertEquals(Range.AttributeRange.UntrackedAttr, result,
                "Expected UntrackedAttr when parent is null");
    }

    @Test
    @DisplayName("sourceRange delegates to parent.sourceRange when parent is non-null (parent!=null branch)")
    public void test_TC02() {
        // Given a stubbed Attributes where sourceRange returns a sentinel value, and non-null parent
        Range.AttributeRange sentinel = Range.AttributeRange.UntrackedAttr;
        Attributes stub = new Attributes() {
            @Override
            public Range.AttributeRange sourceRange(String key) {
                return sentinel;
            }
        };
        Attribute attr = new Attribute("data-key", "val", stub);
        // Parent is non-null so B0→B2 path is taken, delegating to stub.sourceRange
        Range.AttributeRange result = attr.sourceRange();
        // Expect exactly the stub return value
        assertEquals(sentinel, result,
                "Expected delegation to parent.sourceRange when parent is non-null");
    }
}