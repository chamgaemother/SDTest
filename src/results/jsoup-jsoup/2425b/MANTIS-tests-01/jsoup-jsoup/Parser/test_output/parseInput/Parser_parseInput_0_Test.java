package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_0_Test {

    // A minimal stub TreeBuilder to return a predefined Document or throw
    static abstract class StubTreeBuilder extends TreeBuilder {
        @Override
        public ParseSettings defaultSettings() {
            return new ParseSettings(true, true);
        }
        @Override
        public TreeBuilder newInstance() {
            return this;
        }
        @Override
        public String defaultNamespace() {
            return "";
        }
        @Override
        public TagSet defaultTagSet() {
            return TagSet.Html();
        }
        @Override
        public abstract void process(Token token); // Changed to abstract
    }

    @Test
    @DisplayName("TC01_O1: parseInput(String, String) with valid HTML and baseUri returns Document after lock parse release")
    public void test_TC01_O1() {
        // GIVEN a stub builder that returns a predefined Document
        Document predefinedDoc = new Document("http://example.com");
        StubTreeBuilder stubBuilder = new StubTreeBuilder() {
            @Override
            public Document parse(Reader input, String baseUri, Parser parser) {
                // branch B1->B2->B3: normal parse path
                return predefinedDoc;
            }
            @Override
            public void process(Token token) {} // Implemented process method
        };
        Parser parser = new Parser(stubBuilder);
        String html = "<p>text</p>";
        String baseUri = "http://example.com";
        // WHEN calling the String overload
        Document result = parser.parseInput(html, baseUri);
        // THEN assert the returned instance is exactly the stub's Document
        // Inline comment: html non-null so reaches lock-parse-unlock path
        assertSame(predefinedDoc, result, "Expected the stub document instance to be returned");
    }

    @Test
    @DisplayName("TC02_O2: parseInput(Reader, String) with valid Reader and baseUri returns Document after lock parse release")
    public void test_TC02_O2() {
        // GIVEN a stub builder that returns a predefined Document
        Document predefinedDoc = new Document("http://test");
        StubTreeBuilder stubBuilder = new StubTreeBuilder() {
            @Override
            public Document parse(Reader input, String baseUri, Parser parser) {
                // branch B1->B2->B3: normal parse path
                return predefinedDoc;
            }
            @Override
            public void process(Token token) {} // Implemented process method
        };
        Parser parser = new Parser(stubBuilder);
        Reader input = new StringReader("<div></div>");
        String baseUri = "http://test";
        // WHEN calling the Reader overload
        Document result = parser.parseInput(input, baseUri);
        // THEN the stub document instance is returned
        // Inline comment: valid reader so lock and parse succeed
        assertSame(predefinedDoc, result, "Expected the stub document instance to be returned");
    }

    @Test
    @DisplayName("TC03_O1: parseInput(String, String) with null html throws NullPointerException before locking")
    public void test_TC03_O1() {
        // GIVEN an HTML parser and null html
        Parser parser = Parser.htmlParser();
        String html = null;
        String baseUri = "http://a";
        // WHEN & THEN: new StringReader(null) should throw NPE immediately (before lock)
        // Inline comment: passing null to StringReader triggers NPE at constructor
        assertThrows(NullPointerException.class, () -> parser.parseInput(html, baseUri));
    }

    @Test
    @DisplayName("TC04_O2: parseInput(Reader, String) when TreeBuilder.parse throws UncheckedIOException unlocks and propagates exception")
    public void test_TC04_O2() {
        // GIVEN a stub builder that throws UncheckedIOException
        UncheckedIOException ioException = new UncheckedIOException(new java.io.IOException("io fail"));
        StubTreeBuilder stubBuilder = new StubTreeBuilder() {
            @Override
            public Document parse(Reader input, String baseUri, Parser parser) {
                // branch B1->B2->B3: parse call throws
                throw ioException;
            }
            @Override
            public void process(Token token) {} // Implemented process method
        };
        Parser parser = new Parser(stubBuilder);
        Reader input = new StringReader("data");
        String baseUri = "uri";
        // WHEN & THEN: the UncheckedIOException is propagated
        // Inline comment: Reader overload, stub parse throws in try, finally unlocks
        UncheckedIOException thrown = assertThrows(UncheckedIOException.class,
            () -> parser.parseInput(input, baseUri));
        assertSame(ioException, thrown, "Expected the same UncheckedIOException instance to be propagated");
    }
}