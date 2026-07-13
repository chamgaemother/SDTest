package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.nio.charset.Charset;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Document.clone() method scenarios TC01 to TC04.
 */
public class Document_clone_0_Test {

    @Test
    @DisplayName("clone() on empty Document returns a distinct instance with identical base state")
    public void test_TC01() {
        // GIVEN an empty Document with no children, default settings and parser
        Document doc = new Document("http://example.com");
        // WHEN clone() is called
        Document copy = doc.clone();
        // THEN the clone is not the same instance but shares base URI, no children, identical settings and parser state
        assertAll("Empty document deep clone checks",
            () -> assertNotSame(doc, copy, "Clone should be a distinct instance"),
            () -> assertEquals("http://example.com", copy.baseUri(), "Base URI must be copied"),
            () -> assertEquals(0, copy.childNodeSize(), "No child nodes should be present in clone"),
            // Compare output settings state since OutputSettings doesn't override equals
            () -> {
                Document.OutputSettings origOs = doc.outputSettings();
                Document.OutputSettings copyOs = copy.outputSettings();
                assertNotSame(origOs, copyOs, "OutputSettings instances must be distinct");
                assertEquals(origOs.charset(), copyOs.charset(), "Charset should match");
                assertEquals(origOs.escapeMode(), copyOs.escapeMode(), "EscapeMode should match");
                assertEquals(origOs.syntax(), copyOs.syntax(), "Syntax should match");
            },
            // Compare parser state by class and default namespace
            () -> {
                Parser origParser = doc.parser();
                Parser copyParser = copy.parser();
                assertNotSame(origParser, copyParser, "Parser instances must be distinct");
                assertEquals(origParser.getClass(), copyParser.getClass(), "Parser class should match");
                assertEquals(origParser.defaultNamespace(), copyParser.defaultNamespace(), "Default namespace should match");
            }
        );
    }

    @Test
    @DisplayName("clone() deep copies Document with one child node; modifying clone child does not affect original")
    public void test_TC02() {
        // GIVEN a Document shell with one <p> child containing text "para"
        Document doc = Document.createShell("http://base");
        doc.body().appendElement("p").text("para");
        // WHEN clone() is called and the clone's <p> text is modified
        Document copy = doc.clone();
        // inline comment: We exercise deep clone path ensuring child nodes are duplicated (path B0→B1→B2).
        Element copyPara = copy.body().selectFirst("p");
        copyPara.text("new");
        // THEN original is unaffected, clone reflects change
        assertAll("Mutation isolation between original and clone",
            () -> assertEquals("para", doc.body().selectFirst("p").text(),
                "Original document's <p> text should remain unchanged"),
            () -> assertEquals("new", copy.body().selectFirst("p").text(),
                "Clone's <p> text should reflect the update")
        );
    }

    @Test
    @DisplayName("clone() preserves multiple child nodes; each child cloned separately")
    public void test_TC03() {
        // GIVEN a Document shell with two children <div> and <span>
        Document doc = Document.createShell("u");
        doc.body().appendElement("div");
        doc.body().appendElement("span");
        // WHEN clone() is called
        Document copy = doc.clone();
        // inline comment: Validates deep clone over multiple children (loop-N branch).
        assertEquals(doc.childNodeSize(), copy.childNodeSize(),
            "Clone should have same number of top-level child nodes");
        // Check each clamped child is a distinct instance with same tag name
        for (int i = 0; i < doc.childNodeSize(); i++) {
            Node origNode = doc.childNodes().get(i);
            Node copyNode = copy.childNodes().get(i);
            assertAll("Child node " + i + " deep clone",
                () -> assertNotSame(origNode, copyNode, "Each child node instance must be distinct"),
                () -> assertEquals(origNode.nodeName(), copyNode.nodeName(),
                    "Each child node must have the same tag name")
            );
        }
    }

    @Test
    @DisplayName("clone() returns deep copy such that modifying outputSettings of clone does not affect original")
    public void test_TC04() {
        // GIVEN a Document with default settings
        Document doc = new Document("ns", "base");
        // WHEN clone() is called and clone charset is changed to US-ASCII
        Document copy = doc.clone();
        // inline comment: Ensures deep copy of outputSettings so modifying clone's charset doesn't alter original.
        copy.charset(Charset.forName("US-ASCII"));
        // THEN original remains UTF-8, clone uses US-ASCII
        assertAll("OutputSettings isolation after charset change",
            () -> assertEquals("UTF-8", doc.charset().name(), "Original document charset must remain UTF-8"),
            () -> assertEquals("US-ASCII", copy.charset().name(), "Clone charset must update to US-ASCII")
        );
    }
}