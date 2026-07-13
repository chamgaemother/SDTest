package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.parser.Parser;
import org.jsoup.nodes.XmlDeclaration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Document_charset_1_Test {

    @Test
    @DisplayName("TC06: XML syntax with first child XmlDeclaration name≠'xml' prepends new declaration and leaves existing")
    public void test_TC06() throws Exception {
        // GIVEN a shell document with xml output syntax and an existing non-xml declaration as first child
        Document doc = Document.createShell("u");
        doc.outputSettings().syntax(OutputSettings.Syntax.xml);
        XmlDeclaration existing = new XmlDeclaration("doc", false);
        // Use reflection to invoke private prependChild so existing decl is first
        Method prepend = Document.class.getDeclaredMethod("prependChild", Node.class);
        prepend.setAccessible(true);
        prepend.invoke(doc, existing);
        // WHEN charset is set -> triggers ensureMetaCharsetElement for xml branch where first child is XmlDeclaration with wrong name
        doc.charset(Charset.forName("UTF-16LE"));
        // THEN a new xml declaration is prepended, preserving existing
        List<Node> children = doc.childNodes();
        assertFalse(children.isEmpty(), "Document should have child nodes after charset");
        // first node must be newly prepended xml declaration
        Node firstNode = children.get(0);
        assertTrue(firstNode instanceof XmlDeclaration, "First child must be XmlDeclaration");
        XmlDeclaration firstDecl = (XmlDeclaration) firstNode;
        assertEquals("xml", firstDecl.name(), "New declaration name should be 'xml'");
        assertEquals("UTF-16LE", firstDecl.attr("encoding"), "New declaration encoding attribute");
        assertEquals("1.0", firstDecl.attr("version"), "New declaration version attribute");
        // second node is the existing non-xml declaration
        Node secondNode = children.get(1);
        assertTrue(secondNode instanceof XmlDeclaration, "Second child should remain the existing XmlDeclaration");
        XmlDeclaration secondDecl = (XmlDeclaration) secondNode;
        assertEquals("doc", secondDecl.name(), "Existing declaration name should remain 'doc'");
    }

    @Test
    @DisplayName("TC07: Calling charset(null) throws NullPointerException")
    public void test_TC07() {
        // GIVEN a fresh shell document without any special setup
        Document doc = Document.createShell("http://example.com");
        // WHEN/THEN charset(null) should immediately NPE (Validate.notNull)
        assertThrows(NullPointerException.class, () -> doc.charset((Charset) null),
                "charset(null) should throw NullPointerException");
    }
}