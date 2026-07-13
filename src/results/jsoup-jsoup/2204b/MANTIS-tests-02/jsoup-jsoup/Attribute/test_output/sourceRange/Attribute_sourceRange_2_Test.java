package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Attribute#sourceRange method.
 */
public class Attribute_sourceRange_2_Test {

    @Test
    @DisplayName("sourceRange delegates to parent.sourceRange when parent is non-null")
    public void test_TC02() {
        // Arrange: create a stub Range.AttributeRange to be returned by our stub parent.
        Range.AttributeRange stubRange = new Range.AttributeRange(new Range(5), new Range(15)); // Fixed constructor parameters
        // Arrange: define a stub Attributes subclass overriding sourceRange.
        Attributes stubParent = new Attributes() {
            @Override
            public Range.AttributeRange sourceRange(String key) {
                return new Range.AttributeRange(new Range(5), new Range(15));  // Fixed constructor parameters
            }
        };
        // Arrange: create Attribute with non-null parent so delegation branch is taken (parent != null).
        Attribute attr = new Attribute("myKey", "myVal", stubParent);

        // Act: invoke sourceRange, expecting delegation to stubParent.sourceRange
        Range.AttributeRange result = attr.sourceRange();

        // Assert: the returned object is exactly the stubRange we provided.
        assertSame(stubRange, result, 
            "Expected sourceRange() to return the same Range.AttributeRange instance provided by the parent");
    }
}