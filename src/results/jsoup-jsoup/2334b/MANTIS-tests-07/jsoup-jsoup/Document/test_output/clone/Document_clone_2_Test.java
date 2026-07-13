package org.jsoup.nodes;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Document.clone() method, covering deep-copy behavior and empty document cloning.
 */
public class Document_clone_2_Test {

    @Test
    @DisplayName("clone() deep-copies custom child nodes: modifying clone’s root children does not affect original")
    void test_TC05() {
        // GIVEN a Document with one <div> child under the root
        Document original = new Document("html", "http://example.com");
        Element div = original.appendElement("div");
        div.text("hello");
        // WHEN cloning and mutating the clone: remove the cloned div and add a <span>
        Document copy = original.clone();
        // Remove the first child node of the clone (the <div>)
        copy.childNode(0).remove(); // ensures branch B3 of clone where children exist
        // Add a new <span> to the clone
        copy.appendElement("span"); // ensures clone has independent new node list
        // THEN original still has its div and no span
        Elements origDivs = original.select("div");
        Elements origSpans = original.select("span");
        assertAll(
            () -> assertEquals(1, origDivs.size(), "Original should retain its <div> child"),
            () -> assertTrue(origSpans.isEmpty(), "Original should have no <span> child after clone mutation")
        );
        // AND clone has only span and no div
        Elements copyDivs = copy.select("div");
        Elements copySpans = copy.select("span");
        assertAll(
            () -> assertTrue(copyDivs.isEmpty(), "Clone should no longer have the <div> child"),
            () -> assertEquals(1, copySpans.size(), "Clone should have the newly added <span> child")
        );
    }

    @Test
    @DisplayName("clone() on an empty Document yields an independent document with no child nodes")
    void test_TC06() {
        // GIVEN an empty Document with no appended children
        Document original = new Document("html", "http://empty");
        // WHEN cloning
        Document copy = original.clone();
        // THEN the clone is a distinct instance
        assertNotSame(original, copy, "Clone should be a different instance than original");
        // AND both have zero child nodes
        assertAll(
            () -> assertEquals(0, original.childNodeSize(), "Original should have no child nodes"),
            () -> assertEquals(0, copy.childNodeSize(), "Clone should have no child nodes as well")
        );
    }
}