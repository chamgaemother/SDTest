package org.jsoup.nodes;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
public class Document_clone_2_Test {

    @Test
    @DisplayName("TC10: shallowClone() when attributes is null skips attribute‐copying branch and clones minimal state")
    public void test_TC10() throws Exception {
        // GIVEN a new Document with no attributes added so its attributes field stays null
        Document doc = new Document("ns", "http://u");
        // verify precondition: attributes not yet initialized
        Field attrsField = Document.class.getSuperclass().getDeclaredField("attributes");
        attrsField.setAccessible(true);
        assertNull(attrsField.get(doc), "Precondition failed: attributes field should be null");

        // WHEN cloning shallowly
        Document clone = doc.shallowClone();

        // THEN the clone is a distinct instance
        assertNotSame(doc, clone, "Clone should not be the same instance as original");
        // AND retains the same baseUri
        assertEquals(doc.baseUri(), clone.baseUri(), "Base URI should be preserved in shallow clone");
        // AND a non-null outputSettings is cloned
        assertNotNull(clone.outputSettings(), "OutputSettings must be cloned and not null");
        assertNotSame(doc.outputSettings(), clone.outputSettings(), "OutputSettings object must be distinct");
        // AND attributes remains null on clone, skipping attribute-copying branch
        assertNull(attrsField.get(clone), "Attributes should remain null when original had none");
    }

    @Test
    @DisplayName("TC11: shallowClone() when attributes is non-null exercises attribute‐copying branch")
    public void test_TC11() throws Exception {
        // GIVEN a Document shell with at least one attribute on its root element
        Document doc = Document.createShell("http://u");
        // add an attribute so attributes becomes non-null, triggering the copy branch
        doc.attr("data-test", "val");
        // reflection to access the protected attributes field from Element superclass
        Field attrsField = Document.class.getSuperclass().getDeclaredField("attributes");
        attrsField.setAccessible(true);
        Object origAttrs = attrsField.get(doc);
        assertNotNull(origAttrs, "Precondition failed: attributes should be initialized after setting one");

        // WHEN cloning shallowly
        Document clone = doc.shallowClone();

        // THEN the cloned attributes object is a distinct copy
        Object clonedAttrs = attrsField.get(clone);
        assertNotNull(clonedAttrs, "Cloned attributes should not be null when original had attributes");
        assertNotSame(origAttrs, clonedAttrs, "Attributes map must be a new distinct object");
        // AND the attribute data-test=val is preserved in clone
        assertEquals("val", clone.attr("data-test"), "Cloned document should preserve attribute values");
    }
}