package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link Attribute#sourceRange()} focusing on delegation behavior.
 */
public class Attribute_sourceRange_2_Test {

    @Test
    @DisplayName("sourceRange() delegates to parent.sourceRange(key) when parent is non-null (parent != null branch)")
    public void test_TC02() {
        // GIVEN: a stubbed Attributes parent that returns a known AttributeRange instance
        String key = "k";
        Range.AttributeRange expected = new Range.AttributeRange(5, 10); // Changed to match constructor
        Attributes stubParent = new Attributes() {
            @Override
            public Range.AttributeRange sourceRange(String k) {
                // ensure this override is used when parent is non-null
                return expected;
            }

            // stub other abstract or used methods minimally
            @Override public int size() { return 0; }
            @Override public boolean isEmpty() { return true; }
            @Override public boolean containsKey(Object key) { return false; }
            @Override public boolean containsValue(Object value) { return false; }
            @Override public String get(Object key) { return null; }
            @Override public String put(String key, String value) { return null; }
            @Override public String remove(Object key) { return null; }
            @Override public void putAll(java.util.Map<? extends String, ? extends String> m) {}
            @Override public void clear() {}
            @Override public java.util.Set<String> keySet() { return new java.util.HashSet<>(); }
            @Override public java.util.Collection<String> values() { return new java.util.ArrayList<>(); }
            @Override public java.util.Set<java.util.Map.Entry<String, String>> entrySet() { return new java.util.HashSet<>(); }
            @Override public int indexOfKey(String key) { return -1; }
            @Override public java.util.Map<String, Range.AttributeRange> getRanges() { return new java.util.HashMap<>(); }
        };
        Attribute attr = new Attribute(key, "v", stubParent);

        // WHEN: calling sourceRange() with non-null parent
        Range.AttributeRange result = attr.sourceRange();

        // THEN: expect the exact instance returned by parent.sourceRange(key)
        assertSame(expected, result, "Expected delegation to stubbed parent.sourceRange");
    }
}