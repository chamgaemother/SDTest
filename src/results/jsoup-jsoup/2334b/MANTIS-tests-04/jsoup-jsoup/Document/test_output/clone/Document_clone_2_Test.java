package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Parser;
import org.jsoup.nodes.Document.OutputSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;
public class Document_clone_2_Test {

    @Test
    @DisplayName("Modifying original’s outputSettings and parser after clone does not affect the clone (deep copy independence)")
    public void test_TC02() {
        // GIVEN a new document shell with default settings
        Document doc = Document.createShell("/base");
        // WHEN a deep clone is made (should copy settings and parser)
        Document copy = doc.clone();
        // Mutate the original's outputSettings and parser
        doc.outputSettings().charset(Charset.forName("UTF-16")); // change charset on original
        Parser origParser = doc.parser();
        origParser.setTrackErrors(5); // mutate original parser
        doc.parser(origParser);
        // THEN the clone retains default UTF-8 charset and trackErrors=0
        assertEquals("UTF-8", copy.outputSettings().charset().name(),
                "Clone should retain original default UTF-8 charset despite original mutation");
        assertEquals(0, copy.parser().getTrackErrors(),
                "Clone's parser trackErrors should remain at default 0 after original parser was mutated");
        // And the clone's settings and parser are distinct instances (deep copy)
        assertNotSame(doc.outputSettings(), copy.outputSettings(),
                "OutputSettings instances should not be the same between original and clone");
        assertNotSame(doc.parser(), copy.parser(),
                "Parser instances should not be the same between original and clone");
    }

    @Test
    @DisplayName("Modifying clone’s outputSettings and parser after clone does not affect the original (deep copy symmetry)")
    public void test_TC03() {
        // GIVEN a new document shell with default settings
        Document doc = Document.createShell("/doc");
        // WHEN a deep clone is made
        Document copy = doc.clone();
        // Mutate the clone's outputSettings and parser
        copy.outputSettings().syntax(Document.OutputSettings.Syntax.xml); // change syntax on clone
        Parser copyParser = copy.parser();
        copyParser.setTrackErrors(7); // mutate clone parser
        copy.parser(copyParser);
        // THEN the original retains default HTML syntax and trackErrors=0
        assertEquals(Document.OutputSettings.Syntax.html, doc.outputSettings().syntax(),
                "Original should retain default HTML syntax despite clone mutation");
        assertEquals(0, doc.parser().getTrackErrors(),
                "Original parser trackErrors should remain at default 0 after clone parser was mutated");
        // And the instances remain distinct
        assertNotSame(doc.outputSettings(), copy.outputSettings(),
                "OutputSettings instances should not be the same between original and clone");
        assertNotSame(doc.parser(), copy.parser(),
                "Parser instances should not be the same between original and clone");
    }

    @Test
    @DisplayName("Clone returns distinct parser and outputSettings instances each time (object identity)")
    public void test_TC04() {
        // GIVEN a document with custom parser and outputSettings
        Parser customParser = Parser.htmlParser().setTrackErrors(2); // custom parser
        Document doc = Document.createShell("/id").parser(customParser);
        OutputSettings customSettings = doc.outputSettings().escapeMode(Entities.EscapeMode.extended); // custom settings
        doc.outputSettings(customSettings);
        // WHEN two clones are created in succession
        Document copy1 = doc.clone();
        Document copy2 = doc.clone();
        // THEN each clone has distinct parser and settings from original and from each other
        assertNotSame(doc.parser(), copy1.parser(),
                "Clone1 parser should not be the same instance as original's parser");
        assertNotSame(doc.outputSettings(), copy1.outputSettings(),
                "Clone1 settings should not be the same instance as original's settings");
        assertNotSame(copy1.parser(), copy2.parser(),
                "Clone2 parser should not be the same instance as Clone1's parser");
        assertNotSame(copy1.outputSettings(), copy2.outputSettings(),
                "Clone2 settings should not be the same instance as Clone1's settings");
    }
}