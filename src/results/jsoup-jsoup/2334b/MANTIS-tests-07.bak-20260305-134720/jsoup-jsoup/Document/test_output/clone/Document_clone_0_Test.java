package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.nio.charset.Charset;
import static org.junit.jupiter.api.Assertions.*;
public class Document_clone_0_Test {

    @Test
    @DisplayName("cloning an empty Document produces a distinct Document with same baseUri, location, quirksMode, no children, and independent outputSettings and parser instances")
    public void test_TC01() {
        // GIVEN: an empty Document with default settings and no children (triggers B0→B1→B2 path in clone)
        Document orig = new Document("http://example.com");
        // WHEN: clone is invoked
        Document copy = orig.clone(); // Ensure clone() method is defined in Document
        // THEN: verify distinct instance and same simple fields
        assertNotSame(orig, copy, "Clone should produce a new instance");
        assertEquals(orig.baseUri(), copy.baseUri(), "Base URI should be copied");
        assertEquals(orig.location(), copy.location(), "Location should be copied");
        assertEquals(orig.quirksMode(), copy.quirksMode(), "Quirks mode should be copied");
        assertEquals(0, copy.childNodeSize(), "Empty document should have no child nodes after clone");
        // deep copies of settings and parser
        assertNotSame(orig.outputSettings(), copy.outputSettings(), "OutputSettings should be a distinct instance");
        assertEquals(orig.outputSettings(), copy.outputSettings(), "OutputSettings should be equal in value");
        assertNotSame(orig.parser(), copy.parser(), "Parser should be a distinct instance");
        assertEquals(orig.parser().getName(), copy.parser().getName(), "Parser names should match");
        // mutate orig to ensure independence
        orig.quirksMode(Document.QuirksMode.limitedQuirks);
        orig.charset(Charset.forName("UTF-16"));
        assertNotEquals(orig.quirksMode(), copy.quirksMode(), "Changing orig quirksMode should not affect copy");
        assertNotEquals(orig.charset(), copy.charset(), "Changing orig charset should not affect copy");
    }

    @Test
    @DisplayName("cloning a Document with attributes and children preserves attribute values and clones children independently")
    public void test_TC02() {
        // GIVEN: a Document with an attribute and one <p> child in body (ensures branches for attributes and children)
        Document orig = Document.createShell("base");
        orig.attr("data-test", "value");
        orig.body().appendElement("p").text("text");
        // WHEN: clone is invoked
        Document copy = orig.clone(); // Ensure clone() method is defined in Document
        // THEN: verify attribute and child text copied
        assertEquals("value", copy.attr("data-test"), "Attribute should be copied to clone");
        assertNotNull(copy.body().selectFirst("p"));
        assertEquals("text", copy.body().selectFirst("p").text(), "Child element text should be copied");
        // mutate original attribute and child to ensure clone independence
        orig.attr("data-test", "new");
        orig.body().selectFirst("p").text("changed");
        assertEquals("value", copy.attr("data-test"), "Modifying orig attribute should not affect clone");
        assertEquals("text", copy.body().selectFirst("p").text(), "Modifying orig child text should not affect clone");
    }

    @Test
    @DisplayName("cloning preserves custom quirksMode and then original change of quirksMode does not affect clone")
    public void test_TC03() {
        // GIVEN: a Document with custom quirks mode set to limitedQuirks (covers branch copying quirksMode)
        Document orig = new Document("u");
        orig.quirksMode(Document.QuirksMode.limitedQuirks);
        // WHEN: clone is invoked
        Document copy = orig.clone(); // Ensure clone() method is defined in Document
        // THEN: copy should preserve the limitedQuirks setting
        assertEquals(Document.QuirksMode.limitedQuirks, copy.quirksMode(), "Clone should preserve original quirksMode");
        // mutate original to noQuirks and ensure clone remains unchanged
        orig.quirksMode(Document.QuirksMode.noQuirks);
        assertEquals(Document.QuirksMode.limitedQuirks, copy.quirksMode(), "Changing orig quirksMode should not affect clone");
    }
}