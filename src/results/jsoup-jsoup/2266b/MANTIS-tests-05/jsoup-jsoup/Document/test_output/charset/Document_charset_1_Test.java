package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Document_charset_1_Test {

    @Test
    @DisplayName("HTML doc with existing <meta charset> and obsolete <meta name=charset> updates charset attribute and removes obsolete")
    public void test_TC05() {
        // GIVEN a simple HTML shell with both <meta charset> and obsolete <meta name=charset>
        Document doc = Document.createShell("http://x");
        // append existing valid charset meta
        doc.head().appendElement("meta").attr("charset", "ISO-8859-1");
        // append obsolete charset meta
        doc.head().appendElement("meta").attr("name", "charset");
        Charset newCs = Charset.forName("US-ASCII");
        // WHEN updating charset
        doc.charset(newCs);
        // THEN outputSettings updated
        assertEquals(newCs, doc.outputSettings().charset());
        // existing <meta charset> should have its attribute updated
        Element meta = doc.selectFirst("head meta[charset]");
        assertNotNull(meta);
        assertEquals("US-ASCII", meta.attr("charset"));
        // obsolete <meta name=charset> should be removed
        assertNull(doc.selectFirst("head meta[name=charset]"));
    }

    @Test
    @DisplayName("XML doc with no existing XmlDeclaration prepends new <?xml encoding?> and version")
    public void test_TC06() {
        // GIVEN an HTML shell switched to XML syntax, with no XmlDeclaration child
        Document doc = Document.createShell("u");
        doc.outputSettings().syntax(OutputSettings.Syntax.xml);
        Charset newCs = Charset.forName("UTF-16");
        // WHEN setting charset
        doc.charset(newCs);
        // THEN outputSettings updated
        assertEquals(newCs, doc.outputSettings().charset());
        // first child node should be XmlDeclaration with encoding and version
        List<Node> children = doc.childNodes();
        assertFalse(children.isEmpty());
        Node first = children.get(0);
        assertTrue(first instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) first;
        assertEquals("UTF-16", decl.attr("encoding"));
        assertEquals("1.0", decl.attr("version"));
    }

    @Test
    @DisplayName("XML doc with existing non-xml XmlDeclaration prepends new <?xml encoding?> and version over wrong-name decl")
    public void test_TC07() throws Exception {
        // GIVEN an HTML shell switched to XML syntax, with a wrong-name XmlDeclaration prepended
        Document doc = Document.createShell("v");
        doc.outputSettings().syntax(OutputSettings.Syntax.xml);
        // create a wrong XmlDeclaration and prepend via reflection since prependChild is package-private
        XmlDeclaration wrong = new XmlDeclaration("wrong", false);
        wrong.attr("foo", "bar");
        Method prepend = Node.class.getDeclaredMethod("prependChild", Node.class);
        prepend.setAccessible(true);
        prepend.invoke(doc, wrong);
        Charset newCs = Charset.forName("UTF-8");
        // WHEN setting charset
        doc.charset(newCs);
        // THEN the first child should be a corrected XmlDeclaration
        List<Node> children = doc.childNodes();
        assertFalse(children.isEmpty());
        Node first = children.get(0);
        assertTrue(first instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) first;
        assertEquals("UTF-8", decl.attr("encoding"));
        assertEquals("1.0", decl.attr("version"));
    }

    @Test
    @DisplayName("Calling charset(null) on Document throws NullPointerException and leaves charset unchanged")
    public void test_TC08() {
        // GIVEN a document and its original charset
        Document doc = Document.createShell("n");
        Charset before = doc.outputSettings().charset();
        // WHEN/THEN setting null should NPE and not change charset
        assertThrows(NullPointerException.class, () -> doc.charset(null));
        assertEquals(before, doc.outputSettings().charset());
    }

    @Test
    @DisplayName("HTML doc with updateMetaCharsetElement disabled does not insert or update <meta charset>")
    public void test_TC09() {
        // GIVEN a shell with updateMetaCharsetElement turned off
        Document doc = Document.createShell("z");
        doc.updateMetaCharsetElement(false);
        Charset newCs = Charset.forName("UTF-8");
        // WHEN setting charset
        doc.charset(newCs);
        // THEN outputSettings updated but no meta[charset] inserted
        assertEquals(newCs, doc.outputSettings().charset());
        assertNull(doc.selectFirst("meta[charset]"));
    }
}