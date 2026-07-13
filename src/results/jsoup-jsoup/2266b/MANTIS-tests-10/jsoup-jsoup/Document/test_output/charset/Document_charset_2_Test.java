package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for Document.charset(Charset) method based on provided scenarios.
 */
public class Document_charset_2_Test {

    @Test
    @DisplayName("TC09: charset(null) throws NullPointerException for null charset argument")
    public void test_TC09() {
        // GIVEN a fresh document shell, updateMetaCharsetElement is false by default
        Document doc = Document.createShell("http://example.com");
        // WHEN & THEN: calling charset(null) should throw NullPointerException as null is invalid
        assertThrows(NullPointerException.class, () -> doc.charset(null));
        // Inline comment: Passing null charset should not be allowed and must result in NPE (branch B2→B3)
    }

    @Test
    @DisplayName("TC10: charset(xml syntax, updateMetaCharsetElement false) updates settings only without adding XmlDeclaration")
    public void test_TC10() throws Exception {
        // GIVEN a fresh document shell with XML syntax and updateMetaCharsetElement(false)
        Document doc = Document.createShell("http://example.com");
        doc.outputSettings().syntax(OutputSettings.Syntax.xml);
        // Default updateMetaCharsetElement flag is false, so charset() should only change the setting and not prepend an XmlDeclaration

        // WHEN: set charset to ISO-8859-1
        Charset target = Charset.forName("ISO-8859-1");
        doc.charset(target);

        // THEN: outputSettings.charset() is updated to ISO-8859-1
        assertEquals(target, doc.outputSettings().charset(), 
            "Expected charset to be updated on outputSettings (branch B0→B1 skip meta element creation)");

        // AND: ensureMetaCharsetElement should NOT have prepended an XmlDeclaration as updateMetaCharsetElement was false
        // Use reflection to access protected field childNodes from superclass Node
        Field childNodesField = Node.class.getDeclaredField("childNodes");
        childNodesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Node> childNodes = (List<Node>) childNodesField.get(doc);

        // The first node should be the html element, not an XmlDeclaration
        assertFalse(childNodes.get(0) instanceof XmlDeclaration, 
            "Expected no XmlDeclaration prepended when updateMetaCharsetElement is false");
    }
}