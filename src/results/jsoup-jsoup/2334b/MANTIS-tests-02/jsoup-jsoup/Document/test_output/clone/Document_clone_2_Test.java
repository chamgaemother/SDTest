package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Document_clone_2_Test {

    @Test
    @DisplayName("TC13: clone() copies multiple root attributes and iterates attr-copy loop for N>1")
    public void test_TC13() throws Exception {
        // GIVEN a document with two root attributes (tests attr-loop x2)
        Document doc = new Document("u");
        doc.attr("k1", "v1");
        doc.attr("k2", "v2");
        
        // WHEN clone is invoked (enters clone override, copies attributes)
        Document clone = doc.clone();
        
        // THEN clone should have both attributes with identical values
        assertEquals("v1", clone.attr("k1"));
        assertEquals("v2", clone.attr("k2"));
        
        // AND the attribute storage instances must differ (distinct attribute instances)
        Field attrsField = Element.class.getDeclaredField("attributes");
        attrsField.setAccessible(true);
        Object origAttrs = attrsField.get(doc);
        Object cloneAttrs = attrsField.get(clone);
        assertNotNull(origAttrs);
        assertNotNull(cloneAttrs);
        assertNotSame(origAttrs, cloneAttrs);
    }

    @Test
    @DisplayName("TC14: clone() deep-clones mixed child nodes preserving order across child-copy loop")
    public void test_TC14() {
        // GIVEN a document with TextNode, Comment, Element children in that order (tests child-loop x3)
        Document doc = new Document("u");
        doc.appendText("txt");                          // text node child
        doc.appendChild(new Comment("cmt"));     // corrected constructor call
        doc.appendElement("div");                      // element child
        
        // WHEN clone is invoked (deep copy of children)
        Document clone = doc.clone();
        
        // THEN clone should have exactly 3 children in same order and types
        List<Node> children = clone.childNodes();
        assertEquals(3, children.size());               // 3 children copied
        assertTrue(children.get(0) instanceof TextNode);
        assertTrue(children.get(1) instanceof Comment);
        assertTrue(children.get(2) instanceof Element);
        
        // AND the element instance should be distinct from original
        assertNotSame(doc.childNodes().get(2), children.get(2));
    }

    @Test
    @DisplayName("TC15: clone() isolation: modifying clone’s nested element attr does not affect original")
    public void test_TC15() {
        // GIVEN a document with nested a > b hierarchy (child-loop x2)
        Document doc = new Document("u");
        Element a = doc.appendElement("a");
        Element b = a.appendElement("b");
        
        // WHEN clone is invoked and clone's 'b' element is mutated
        Document clone = doc.clone();
        Element cloneB = clone.selectFirst("b");
        cloneB.attr("x", "y");                        // mutate clone only
        
        // THEN original should not have attribute 'x'
        Element origB = doc.selectFirst("b");
        assertFalse(origB.hasAttr("x"));              // isolation check
        assertEquals("y", cloneB.attr("x"));         // clone has the new attr
    }

    @Test
    @DisplayName("TC16: clone() clones XmlDeclaration child via child-copy loop when present")
    public void test_TC16() {
        // GIVEN a document with a prepended XmlDeclaration (child-loop x1)
        Document doc = new Document("u");
        XmlDeclaration decl = new XmlDeclaration("xml", false);
        decl.attr("encoding", "UTF-8");
        doc.prependChild(decl);
        
        // WHEN clone is invoked (should copy XmlDeclaration)
        Document clone = doc.clone();
        List<Node> origNodes = doc.childNodes();
        List<Node> cloneNodes = clone.childNodes();
        
        // THEN first child in clone is XmlDeclaration distinct from original
        Node origFirst = origNodes.get(0);
        Node cloneFirst = cloneNodes.get(0);
        assertTrue(cloneFirst instanceof XmlDeclaration);
        assertEquals(((XmlDeclaration) origFirst).name(), ((XmlDeclaration) cloneFirst).name());
        assertNotSame(origFirst, cloneFirst);
    }
}