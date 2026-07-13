package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for {@link Attribute#sourceRange()} method covering both null and non-null parent cases.
 */
public class Attribute_sourceRange_0_Test {

    @Test
    @DisplayName("When parent is null, sourceRange returns UntrackedAttr (covers branch parent==null)")
    public void test_TC01() {
        // Given an Attribute with null parent to force the parent==null branch (B0→B1)
        Attribute a = new Attribute("key", "val", null);
        // When
        Range.AttributeRange result = a.sourceRange();
        // Then: should return the static UntrackedAttr singleton
        assertSame(Range.AttributeRange.UntrackedAttr, result,
                "Expected UntrackedAttr when parent is null");
    }

    @Test
    @DisplayName("When parent is non-null, sourceRange delegates to parent.sourceRange(key) (covers branch parent!=null)")
    public void test_TC02() {
        // Given a stubbed Attributes that returns a known Range.AttributeRange for the given key,
        // to exercise the parent!=null branch (B0→B2)
        final Range.AttributeRange expected = new Range.AttributeRange(1, 2); // Ensure correct types are used
        Attributes stubParent = new Attributes() {
            @Override
            public Range.AttributeRange sourceRange(String k) {
                // Return the expected stubbed range regardless of input
                return expected;
            }
        };
        Attribute a = new Attribute("k", "v", stubParent);
        // When
        Range.AttributeRange result = a.sourceRange();
        // Then: should return exactly the stubbed instance
        assertSame(expected, result,
                "Expected delegate sourceRange result when parent is non-null");
    }
}