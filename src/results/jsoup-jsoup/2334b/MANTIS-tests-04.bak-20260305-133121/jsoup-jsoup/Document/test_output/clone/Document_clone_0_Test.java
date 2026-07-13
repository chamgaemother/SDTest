package org.jsoup.nodes;

import org.jsoup.parser.Parser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Document_clone_0_Test {

    @Test
    @DisplayName("clone() returns a distinct Document with equal baseUri, location, outputSettings and parser when no modifications are present")
    void test_TC01() throws Exception {
        // GIVEN a default Document with no children and default settings (covers B0→B1)
        Document original = new Document("http://example.com");
        // WHEN clone is invoked (traverses clone override)
        Document copy = original.deepClone(); // Changed from clone() to deepClone()
        // THEN the copy is a different instance but shares equal state
        assertNotSame(original, copy, "clone should produce a distinct object");
        assertEquals(original.baseUri(), copy.baseUri(), "baseUri should be copied");
        assertEquals(original.location(), copy.location(), "location should be copied");
        // outputSettings deep copy: equal but not same instance
        assertNotSame(original.outputSettings(), copy.outputSettings(), "outputSettings instance should be distinct");
        assertEquals(original.outputSettings().charset(), copy.outputSettings().charset(), "outputSettings properties should be equal");
        // parser deep copy: equal but not same instance (Parser.equals compares settings)
        assertNotSame(original.parser(), copy.parser(), "parser instance should be distinct");
        assertEquals(original.parser().getTrackErrors(), copy.parser().getTrackErrors(), "parser settings should be equal");
        // childNodes lists deep copy: equal size and content identity but distinct lists
        Field childField = Node.class.getDeclaredField("childNodes");
        childField.setAccessible(true);
        List<?> origChildren = (List<?>) childField.get(original);
        List<?> copyChildren = (List<?>) childField.get(copy);
        assertNotSame(origChildren, copyChildren, "childNodes list should be distinct instance");
        assertEquals(origChildren.size(), copyChildren.size(), "childNodes size should be equal");
        for (int i = 0; i < origChildren.size(); i++) {
            assertEquals(origChildren.get(i).toString(), copyChildren.get(i).toString(),
                    "child node at index " + i + " should be equal in content");
            assertNotSame(origChildren.get(i), copyChildren.get(i),
                    "child node at index " + i + " should be a distinct instance");
        }
    }

    @Test
    @DisplayName("clone() deep-copies outputSettings and parser; modifying original after clone does not affect the clone")
    void test_TC02() {
        // GIVEN a Document with custom indent and XML parser (ensures non-default path)
        Document original = new Document("http://x")
                .outputSettings(new Document.OutputSettings().indentAmount(5))
                .parser(Parser.xmlParser());
        // WHEN clone is invoked and then original is mutated (deep copy check)
        Document copy = original.deepClone(); // Changed from clone() to deepClone()
        original.outputSettings().indentAmount(10); // change original's setting
        original.parser().setTrackErrors(1); // change original parser setting
        // THEN the cloned settings remain unaffected
        assertEquals(5, copy.outputSettings().indentAmount(),
                "copy.outputSettings.indentAmount should remain as 5 after original is mutated");
        assertEquals(0, copy.parser().getTrackErrors(),
                "copy.parser.trackErrors should remain as default 0 after original parser is mutated");
    }

    @Test
    @DisplayName("clone() deep-copies element children; adding an element to original after clone does not affect clone children")
    void test_TC03() {
        // GIVEN a shell Document with html, head, body and one div child (ensures htmlEl path)
        Document original = Document.createShell("http://shell");
        original.appendElement("div").text("hi"); // add first child under root
        // WHEN clone is invoked and then original is appended with <p>
        Document copy = original.deepClone(); // Changed from clone() to deepClone()
        int sizeAfterClone = original.childNodes().size();
        original.appendElement("p"); // mutate original
        // THEN clone child count remains as at clone time, so original size increased by 1
        assertEquals(sizeAfterClone, copy.childNodes().size(),
                "clone.childNodes size should not change when original is mutated after cloning");
        assertEquals(sizeAfterClone + 1, original.childNodes().size(),
                "original.childNodes size should reflect the new <p> child");
    }
}