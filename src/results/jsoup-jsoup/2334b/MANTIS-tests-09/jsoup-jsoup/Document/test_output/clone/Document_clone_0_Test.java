package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;
public class Document_clone_0_Test {

    @Test
    @DisplayName("TC01: clone() on empty Document returns a distinct Document instance with same baseUri, empty childNodes, cloned outputSettings and parser")
    public void test_TC01() {
        // GIVEN an empty Document with no child nodes
        Document original = new Document("http://example.com");

        // WHEN cloning the document
        Document result = original.clone(); // Changed from deepClone() to clone()

        // THEN the clone should be a different instance
        assertAll("verify distinct clone with duplicated configuration",
            () -> assertNotSame(original, result, "Clone should not be the same reference as original"),
            () -> assertEquals(original.baseUri(), result.baseUri(), "Base URI should be preserved"),
            // empty document has no children
            () -> assertTrue(result.childNodes().isEmpty(), "Cloned document should have no child nodes"),
            // outputSettings and parser must be new instances
            () -> assertNotSame(original.outputSettings(), result.outputSettings(), "OutputSettings should be cloned, not shared"),
            () -> assertNotSame(original.parser(), result.parser(), "Parser should be cloned, not shared")
        );
    }

    @Test
    @DisplayName("TC02: clone() on Document with nested children clones the full node tree but returns independent nodes")
    public void test_TC02() {
        // GIVEN a document shell with a <p>Hello</p> in its body
        Document original = Document.createShell("http://foo");
        original.body().appendElement("p").text("Hello");

        // WHEN cloning the document with children
        Document result = original.clone(); // Changed from deepClone() to clone()

        // THEN the clone should be independent but carry the same content
        Element originalP = original.select("p").first();
        Element clonedP = result.select("p").first();
        assertAll("verify deep clone of nodes",
            () -> assertNotSame(original, result, "Clone should not be the same reference as original"),
            () -> assertNotNull(clonedP, "Cloned <p> element should exist"),
            () -> assertEquals("Hello", clonedP.text(), "Cloned <p> text should match original"),
            () -> assertNotSame(originalP, clonedP, "Cloned <p> element should be a distinct instance")
        );
    }

    @Test
    @DisplayName("TC03: clone() ensures modifications to clone.outputSettings do not affect original.outputSettings")
    public void test_TC03() {
        // GIVEN a fresh document with default outputSettings.indentAmount == 1
        Document original = new Document("u");

        // WHEN cloning and modifying the clone's indent amount
        Document result = original.clone(); // Changed from deepClone() to clone()
        result.outputSettings().indentAmount(5); // change only clone

        // THEN modifying clone should not change original
        assertAll("verify outputSettings independence",
            () -> assertEquals(1, original.outputSettings().indentAmount(), "Original indentAmount should remain default"),
            () -> assertEquals(5, result.outputSettings().indentAmount(), "Clone indentAmount should reflect change")
        );
    }

    @Test
    @DisplayName("TC04: clone() ensures modifications to clone.parser do not affect original.parser")
    public void test_TC04() {
        // GIVEN a fresh document whose parser has no tracked errors
        Document original = new Document("u");
        assertTrue(original.parser().getErrors().isEmpty(), "Original parser should start with no errors");

        // WHEN cloning and setting trackErrors on the clone parser
        Document result = original.clone(); // Changed from deepClone() to clone()
        result.parser().setTrackErrors(10);

        // THEN original parser errors list remains unaffected
        assertAll("verify parser independence",
            () -> assertTrue(original.parser().getErrors().isEmpty(), "Original parser errors should remain empty"),
            () -> assertEquals(10, result.parser().getTrackErrors(), "Clone parser should have its own trackErrors value")
        );
    }
}