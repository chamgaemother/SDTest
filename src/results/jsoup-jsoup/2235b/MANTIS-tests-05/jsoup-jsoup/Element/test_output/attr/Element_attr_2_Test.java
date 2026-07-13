package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Attributes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for org.jsoup.nodes.Element#attr(String, String)
 */
public class Element_attr_2_Test {

    @Test
    @DisplayName("TC14: attr(String key with internal spaces, String value) preserves key exactly and stores value")
    public void test_TC14() {
        // GIVEN an Element with no pre-existing attributes
        Element el = new Element("div");
        // WHEN we set an attribute whose key contains an internal space "a b"
        // This exercises the normal path B0->B1->B2 in attr, where the key is taken verbatim.
        el.attr("a b", "v");
        // THEN the Attributes backing map should contain exactly "a b" as key and "v" as value
        Attributes attrs = el.attributes();
        assertEquals("v", attrs.get("a b"),
                "Expected the attribute value for key with internal space to be stored verbatim");
    }

    @Test
    @DisplayName("TC15: attr(String key with leading/trailing whitespace, String value) uses key verbatim without trimming")
    public void test_TC15() {
        // GIVEN an Element with no pre-existing attributes
        Element el = new Element("span");
        // WHEN we set an attribute whose key has leading and trailing whitespace "  k  "
        // This also follows the B0->B1->B2 path, expecting no trimming on the key.
        el.attr("  k  ", "x");
        // THEN the Attributes backing map should contain exactly "  k  " as key and "x" as value
        Attributes attrs = el.attributes();
        assertEquals("x", attrs.get("  k  "),
                "Expected the attribute key with whitespace preserved exactly as provided");
    }
}