package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.XmlDeclaration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Document_clone_2_Test {
    @Test
    @DisplayName("TC07: Modifying original outputSettings after clone does not affect clone’s prettyPrint setting")
    public void test_TC07() {
        // GIVEN a fresh Document with default prettyPrint = true
        Document original = Document.createShell("http://isolated.com");
        assertTrue(original.outputSettings().prettyPrint(), "Precondition: original.prettyPrint should be true");
        // WHEN we clone and then modify the original's prettyPrint to false
        Document copy = original.clone();
        original.outputSettings().prettyPrint(false); // isolate original change
        // THEN the clone's prettyPrint remains true
        assertTrue(copy.outputSettings().prettyPrint(), 
            "Clone's prettyPrint should remain true despite original change");
    }

    @Test
    @DisplayName("TC08: Modifying clone quirksMode does not affect original quirksMode")
    public void test_TC08() {
        // GIVEN a fresh Document with default quirksMode = noQuirks
        Document original = Document.createShell("http://quirks.com");
        assertEquals(Document.QuirksMode.noQuirks, original.quirksMode(), 
            "Precondition: original.quirksMode should be noQuirks");
        // WHEN we clone and then set clone's quirksMode to quirks
        Document copy = original.clone();
        copy.quirksMode(Document.QuirksMode.quirks);
        // THEN original's quirksMode remains unaffected
        assertEquals(Document.QuirksMode.noQuirks, original.quirksMode(),
            "Original quirksMode should remain noQuirks after clone modification");
    }

    @Test
    @DisplayName("TC09: clone() on null-connection Document yields clone.connection() new Session but distinct instances each call")
    public void test_TC09() {
        // GIVEN a Document with no explicit Connection (connection == null)
        Document original = Document.createShell("http://session.com");
        // WHEN we clone and call connection() twice on the clone
        Document copy = original.clone();
        Connection c1 = copy.connection(); // new session
        Connection c2 = copy.connection(); // another new session
        // THEN both connections are non-null and not the same instance
        assertNotNull(c1, "First connection should not be null");
        assertNotNull(c2, "Second connection should not be null");
        assertNotSame(c1, c2, "Each call to connection() should produce a distinct session object");
    }

    @Test
    @DisplayName("TC10: clone() preserves XML syntax in outputSettings when original set to XML and deep-copies XmlDeclaration on ensureMetaCharsetElement")
    public void test_TC10() {
        // GIVEN a Document with syntax set to xml and initial charset applied via ensureMetaCharsetElement
        Document original = Document.createShell("http://xml.com");
        // switch syntax to xml which also sets escape mode to xhtml internally
        original.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        // apply an initial charset to generate an XmlDeclaration node
        original.charset(Charset.forName("ISO-8859-1"));
        List<?> originalChildren = original.childNodes();
        assertFalse(originalChildren.isEmpty() && originalChildren.get(0) instanceof XmlDeclaration == false,
            "Precondition: original should have an XmlDeclaration as first child");
        // WHEN we clone and then call charset() on the clone to trigger ensureMetaCharsetElement
        Document copy = original.clone();
        copy.charset(Charset.forName("UTF-8")); // triggers ensureMetaCharsetElement on xml syntax
        // THEN the clone retains xml syntax and has its own XmlDeclaration distinct from original
        assertEquals(Document.OutputSettings.Syntax.xml, copy.outputSettings().syntax(),
            "Clone should preserve xml syntax in outputSettings");
        List<?> copyChildren = copy.childNodes();
        assertFalse(copyChildren.isEmpty(), "Clone should have child nodes after ensureMetaCharsetElement");
        assertTrue(copyChildren.get(0) instanceof XmlDeclaration,
            "First child of clone should be an XmlDeclaration");
        assertNotSame(original.childNodes().get(0), copyChildren.get(0),
            "XmlDeclaration in clone should be a deep copy, not the same instance as original");
    }
}