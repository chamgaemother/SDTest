package org.jsoup.nodes;

import org.jsoup.Connection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class Document_clone_2_Test {

    /**
     * TEST TC07 clone null-connection branch
     * GIVEN a Document with no connection set (connection field is null),
     * WHEN we clone and call connection() twice,
     * THEN each call returns a new session distinct from each other and from the original document's session.
     */
    @Test
    @DisplayName("clone() on Document with null connection preserves null field so connection() returns new session")
    public void test_TC07() {
        // GIVEN: a new Document has connection==null internally
        Document doc = new Document("http://example.com");
        // original connection() yields a new session because internal field is null
        Connection origSession = doc.connection();
        assertNotNull(origSession, "Original document.connection() should not be null (returns new session)");

        // WHEN: clone the document
        Document cloned = doc.clone();

        // THEN: each connection() call on the clone generates a new session
        Connection c1 = cloned.connection();
        Connection c2 = cloned.connection();
        assertNotNull(c1, "Clone.connection() first call should return a non-null session");
        assertNotNull(c2, "Clone.connection() second call should return a non-null session");
        // c1 and c2 should be distinct sessions
        assertNotSame(c1, c2, "Each call to clone.connection() should produce a new session instance");
        // and should not match the original document's session
        assertNotSame(origSession, c1, "Clone.session should not equal the original document's session");
    }

    /**
     * TEST TC08 clone deep-clones XmlDeclaration
     * GIVEN a Document with an XmlDeclaration prepended as its first child,
     * WHEN we clone,
     * THEN the clone's first child is a distinct XmlDeclaration with the same name.
     */
    @Test
    @DisplayName("clone() deep-copies an existing XmlDeclaration child node")
    public void test_TC08() {
        // GIVEN: a Document and a manually prepended XmlDeclaration
        Document doc = new Document("ns", "http://example.com");
        XmlDeclaration decl = new XmlDeclaration("xml", false);
        doc.prependChild(decl);
        // Sanity check: original first child is our XmlDeclaration
        assertTrue(doc.childNodes().get(0) instanceof XmlDeclaration, "Original first child should be XmlDeclaration");

        // WHEN: clone the document
        Document cloned = doc.clone();

        // THEN: the cloned document's first child is an XmlDeclaration distinct from the original
        assertFalse(cloned.childNodes().isEmpty(), "Cloned document should have at least one child");
        org.jsoup.nodes.Node origNode = doc.childNodes().get(0);
        org.jsoup.nodes.Node copyNode = cloned.childNodes().get(0);

        assertTrue(origNode instanceof XmlDeclaration, "Original node should be XmlDeclaration");
        assertTrue(copyNode instanceof XmlDeclaration, "Cloned node should be XmlDeclaration");
        // Instances should be different
        assertNotSame(origNode, copyNode, "Cloned XmlDeclaration instance should not be the same reference as original");
        // But name should match
        assertEquals(
            ((XmlDeclaration) origNode).name(),
            ((XmlDeclaration) copyNode).name(),
            "Cloned XmlDeclaration should have the same name as original"
        );
    }

    /**
     * TEST TC09 clone independent quirksMode modification
     * GIVEN a Document with default quirksMode noQuirks,
     * WHEN we clone and change the clone's quirksMode,
     * THEN original remains with noQuirks and clone uses the new setting.
     */
    @Test
    @DisplayName("clone() copies default quirksMode and allows independent modification")
    public void test_TC09() {
        // GIVEN: a new Document has quirksMode==noQuirks by default
        Document doc = new Document("http://example.com");
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode(),
            "Original document should start with noQuirks mode");

        // WHEN: clone and modify the clone's quirksMode to quirks
        Document cloned = doc.clone();
        cloned.quirksMode(Document.QuirksMode.quirks);

        // THEN: clone has updated mode, original remains unchanged
        assertEquals(Document.QuirksMode.quirks, cloned.quirksMode(),
            "Cloned document should reflect the newly set quirks mode");
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode(),
            "Original document's quirks mode should remain unchanged after clone modification");
    }
}