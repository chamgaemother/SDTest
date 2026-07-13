package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.XmlDeclaration;
import org.jsoup.Connection;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Document_charset_1_Test {

    @Test
    @DisplayName("charset(Charset) with xml syntax and first child not XmlDeclaration prepends a new xml declaration before existing element")
    public void test_TC05() throws Exception {
        // GIVEN: a shell document with html/head/body, so first child is an Element, not XmlDeclaration
        Document doc = Document.createShell("http://example.com");
        // Set output syntax to xml so ensureMetaCharsetElement chooses xml branch
        doc.outputSettings().syntax(OutputSettings.Syntax.xml);
        Charset cs = StandardCharsets.UTF_8;
        // WHEN: call charset to trigger meta charset update and xml declaration prepend
        doc.charset(cs);
        // THEN: new XmlDeclaration at index 0 with correct attributes
        List<Node> nodes = doc.childNodes();
        assertTrue(nodes.get(0) instanceof XmlDeclaration, "Expected XmlDeclaration at index 0");
        XmlDeclaration decl = (XmlDeclaration) nodes.get(0);
        assertEquals("xml", decl.name(), "Declaration name should be 'xml'");
        assertEquals(cs.displayName(), decl.attr("encoding"), "Encoding attribute should match charset display name");
        assertEquals("1.0", decl.attr("version"), "Version attribute should be '1.0'");
        // Existing html element should now be shifted to index 1
        assertTrue(nodes.get(1) instanceof Element, "Expected html Element at index 1");
        Element html = (Element) nodes.get(1);
        assertEquals("html", html.tagName(), "Element at index 1 should be <html>");
    }

    @Test
    @DisplayName("charset(Charset) with xml syntax and existing XmlDeclaration with non-'xml' name prepends a new xml declaration and leaves old one")
    public void test_TC06() throws Exception {
        // GIVEN: a new Document with an existing XmlDeclaration named 'foo'
        Document doc = new Document("http://example.com");
        // Use reflection to call protected prependChild(Node)
        XmlDeclaration existing = new XmlDeclaration("foo", false);
        existing.attr("version", "0.9");
        existing.attr("encoding", "OLD");
        Method prepend = Document.class.getSuperclass()  // Node class
            .getDeclaredMethod("prependChild", Node.class);
        prepend.setAccessible(true);
        prepend.invoke(doc, existing);
        // Set output syntax to xml to go into xml branch
        doc.outputSettings().syntax(OutputSettings.Syntax.xml);
        Charset cs = StandardCharsets.UTF_16;
        // WHEN: call charset to trigger xml declaration logic
        doc.charset(cs);
        // THEN: a new XmlDeclaration at index 0 with correct attrs
        List<Node> nodes = doc.childNodes();
        Node first = nodes.get(0);
        assertTrue(first instanceof XmlDeclaration, "First node should be a new XmlDeclaration");
        XmlDeclaration newDecl = (XmlDeclaration) first;
        assertEquals("xml", newDecl.name(), "New declaration name should be 'xml'");
        assertEquals(cs.displayName(), newDecl.attr("encoding"), "New declaration encoding should match charset display name");
        assertEquals("1.0", newDecl.attr("version"), "New declaration version should be '1.0'");
        // Old declaration should remain at index 1 unchanged
        Node second = nodes.get(1);
        assertTrue(second instanceof XmlDeclaration, "Second node should be the existing XmlDeclaration");
        XmlDeclaration oldDecl = (XmlDeclaration) second;
        assertEquals("foo", oldDecl.name(), "Old declaration name should remain 'foo'");
        assertEquals("0.9", oldDecl.attr("version"), "Old declaration version should remain '0.9'");
        assertEquals("OLD", oldDecl.attr("encoding"), "Old declaration encoding should remain 'OLD'");
    }
}