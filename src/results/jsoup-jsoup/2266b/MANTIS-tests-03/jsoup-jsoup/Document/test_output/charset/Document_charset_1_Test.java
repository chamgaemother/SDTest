package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Document_charset_1_Test {

    @Test
    @DisplayName("TC06: charset(html syntax) removes existing obsolete meta[name=charset] and appends new meta[charset]")
    public void test_TC06() {
        // GIVEN: a shell document with html syntax, updateMetaCharset true, and head has only obsolete meta[name=charset]
        org.jsoup.nodes.Document doc = org.jsoup.nodes.Document.createShell("base");
        doc.updateMetaCharsetElement(true);
        doc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.html);
        // add obsolete meta[name=charset]
        doc.head().appendElement("meta").attr("name", "charset");
        // WHEN: setting charset should remove obsolete and append new
        doc.charset(Charset.forName("UTF-8"));
        // THEN: exactly one meta[charset] present, none with name=charset
        org.jsoup.select.Elements newMeta = doc.head().select("meta[charset]");
        org.jsoup.select.Elements oldMeta = doc.head().select("meta[name=charset]");
        assertEquals(1, newMeta.size(), "One meta[charset] should be appended");
        assertTrue(oldMeta.isEmpty(), "Obsolete meta[name=charset] should be removed");
    }

    @Test
    @DisplayName("TC07: charset(html syntax) updates existing meta[charset] and removes obsolete meta[name=charset]")
    public void test_TC07() {
        // GIVEN: shell doc with html syntax, updateMetaCharset true, head has both meta[charset]=OLD and meta[name=charset]
        org.jsoup.nodes.Document doc = org.jsoup.nodes.Document.createShell("base");
        doc.updateMetaCharsetElement(true);
        doc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.html);
        // add existing meta[charset] and obsolete meta[name=charset]
        doc.head().appendElement("meta").attr("charset", "OLD");
        doc.head().appendElement("meta").attr("name", "charset");
        // WHEN: set new charset should update existing and remove obsolete
        Charset target = Charset.forName("ISO-8859-1");
        doc.charset(target);
        // THEN: existing meta[charset] has new displayName, obsolete removed
        org.jsoup.nodes.Element m = doc.head().selectFirst("meta[charset]");
        assertNotNull(m, "Existing meta[charset] must be present");
        assertEquals(target.displayName(), m.attr("charset"), "Charset attribute should be updated");
        assertTrue(doc.head().select("meta[name=charset]").isEmpty(), "Obsolete meta[name=charset] should be removed");
    }

    @Test
    @DisplayName("TC08: charset(xml syntax) with no stub prepends new XmlDeclaration to real document child list")
    public void test_TC08() {
        // GIVEN: shell doc, xml syntax, updateMetaCharset true, no childNodes stub covers first-node-not-XmlDeclaration
        org.jsoup.nodes.Document doc = org.jsoup.nodes.Document.createShell("base");
        doc.updateMetaCharsetElement(true);
        doc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
        // WHEN: set charset should prepend XmlDeclaration
        Charset cs = Charset.forName("US-ASCII");
        doc.charset(cs);
        // THEN: first child is XmlDeclaration with correct attrs
        org.jsoup.nodes.Node first = doc.childNodes().get(0);
        assertTrue(first instanceof org.jsoup.nodes.XmlDeclaration, "First node must be XmlDeclaration");
        org.jsoup.nodes.XmlDeclaration decl = (org.jsoup.nodes.XmlDeclaration) first;
        assertEquals("1.0", decl.attr("version"), "XML declaration should have version=1.0");
        assertEquals(cs.displayName(), decl.attr("encoding"), "Encoding attr must match set charset");
    }

    @Test
    @DisplayName("TC09: charset(xml syntax) existing XmlDeclaration named xml but missing version attr skips version insertion")
    public void test_TC09() throws Exception {
        // GIVEN: shell doc, xml syntax, updateMetaCharset true, stub childNodes[0]=XmlDeclaration("xml",false) without version
        org.jsoup.nodes.Document doc = org.jsoup.nodes.Document.createShell("base");
        doc.updateMetaCharsetElement(true);
        doc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
        // stub childNodes via reflection
        Field childField = org.jsoup.nodes.Node.class.getDeclaredField("childNodes");
        childField.setAccessible(true);
        List<org.jsoup.nodes.Node> stubList = new ArrayList<>();
        stubList.add(new org.jsoup.nodes.XmlDeclaration("xml", false));
        childField.set(doc, stubList);
        // WHEN: set charset should update encoding only
        Charset cs = Charset.forName("UTF-8");
        doc.charset(cs);
        // THEN: encoding updated, version remains absent
        org.jsoup.nodes.XmlDeclaration decl = (org.jsoup.nodes.XmlDeclaration) doc.childNodes().get(0);
        assertEquals(cs.displayName(), decl.attr("encoding"), "Only encoding should be updated");
        assertFalse(decl.hasAttr("version"), "Version attribute must remain absent");
    }

    @Test
    @DisplayName("TC10: charset(xml syntax) existing XmlDeclaration with wrong name prepends new declaration before it")
    public void test_TC10() throws Exception {
        // GIVEN: shell doc, xml syntax, updateMetaCharset true, stub childNodes[0]=XmlDeclaration("foo",false)
        org.jsoup.nodes.Document doc = org.jsoup.nodes.Document.createShell("base");
        doc.updateMetaCharsetElement(true);
        doc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
        // stub childNodes with wrong-name declaration
        Field childField = org.jsoup.nodes.Node.class.getDeclaredField("childNodes");
        childField.setAccessible(true);
        List<org.jsoup.nodes.Node> stubList = new ArrayList<>();
        org.jsoup.nodes.XmlDeclaration wrong = new org.jsoup.nodes.XmlDeclaration("foo", false);
        stubList.add(wrong);
        childField.set(doc, stubList);
        // WHEN: setting charset should prepend correct xml declaration
        Charset cs = Charset.forName("UTF-16");
        doc.charset(cs);
        // THEN: first is new xml declaration, original at index1
        org.jsoup.nodes.Node first = doc.childNodes().get(0);
        assertTrue(first instanceof org.jsoup.nodes.XmlDeclaration && ((org.jsoup.nodes.XmlDeclaration) first).name().equals("xml"),
            "First must be new XmlDeclaration with name 'xml'");
        assertSame(wrong, doc.childNodes().get(1), "Original wrong declaration should be shifted to index 1");
    }
}