package org.jsoup.nodes;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Range.AttributeRange;
import org.jsoup.nodes.Attributes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
public class Attribute_sourceRange_0_Test {

    @Test
    @DisplayName("TC01: sourceRange returns UntrackedAttr when parent is null (branch parent==null)")
    public void test_TC01() {
        // GIVEN: An Attribute with parent == null to force the UntrackedAttr branch (B0→B1)
        Attribute attr = new Attribute("key", "value", null);

        // WHEN: Calling sourceRange()
        AttributeRange range = attr.sourceRange();

        // THEN: Should return the sentinel UntrackedAttr constant
        assertSame(AttributeRange.UntrackedAttr, range,
                "Expected UntrackedAttr when parent is null");
    }

    @Test
    @DisplayName("TC02: sourceRange delegates to parent.sourceRange(key) when parent non-null (branch parent!=null)")
    public void test_TC02() {
        // GIVEN: A stubbed Attributes parent that returns a specific AttributeRange
        AttributeRange expected = new AttributeRange(new Range(1, 2)); // Changed to match constructor parameters
        Attributes stubParent = new Attributes() {
            @Override
            public AttributeRange sourceRange(String key) {
                // Return our expected range regardless of key, to exercise branch B0→B2
                return expected;
            }
        };
        // Create the attribute with our stub parent
        Attribute attr = new Attribute("customKey", "someVal", stubParent);

        // WHEN: Calling sourceRange() should invoke stubParent.sourceRange("customKey")
        AttributeRange actual = attr.sourceRange();

        // THEN: We get back exactly the stubbed object
        assertSame(expected, actual,
                "Expected sourceRange to delegate to parent.sourceRange and return the stubbed instance");
    }
}