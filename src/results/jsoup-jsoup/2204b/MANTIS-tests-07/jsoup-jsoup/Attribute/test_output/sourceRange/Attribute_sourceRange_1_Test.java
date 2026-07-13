package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Test class for Attribute.sourceRange method covering branch when parent is non-null.
 */
public class Attribute_sourceRange_1_Test {

    @Test
    @DisplayName("TC02: sourceRange delegates to non-null parent.sourceRange(key) and returns the parent result (branch parent!=null)")
    public void test_TC02() {
        // Arrange: create a stub Attributes with non-null parent and override sourceRange to return a known instance
        final Range.AttributeRange expectedRange = new Range.AttributeRange(1, 2); // Fixed constructor parameters
        Attributes stubParent = new Attributes() {
            @Override
            public Range.AttributeRange sourceRange(String key) {
                // Return the stubbed range regardless of key
                return expectedRange;
            }
        };
        // Provide a non-null parent so that sourceRange should take the delegation branch
        Attribute attr = new Attribute("customKey", "value", stubParent);

        // Act: call sourceRange, expecting delegation to stubParent
        Range.AttributeRange actualRange = attr.sourceRange();

        // Assert: the returned range is exactly the stubbed instance
        assertSame(expectedRange, actualRange, 
            "When parent is non-null, sourceRange should delegate to parent.sourceRange and return its result");
    }
}