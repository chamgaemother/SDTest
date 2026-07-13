package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.ParseErrorList;
import org.jsoup.parser.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_0_Test {

    @Test
    @DisplayName("TC01_O1: parseInput(String, String) with normal HTML and non-empty baseUri delegates to TreeBuilder.parse and returns Document")
    public void test_TC01_O1() {
        Document sentinel = Document.createShell("urn:tc01");
        TreeBuilder stubBuilder = new TreeBuilder() {
            @Override public Document parse(Reader input, String baseUri, Parser parser) {
                return sentinel;
            }
            @Override public List<Node> parseFragment(Reader reader, org.jsoup.nodes.Element context, String baseUri, Parser parser) {
                throw new UnsupportedOperationException();
            }
            @Override public ParseSettings defaultSettings() {
                return new ParseSettings(false, false);
            }
            @Override public TreeBuilder newInstance() {
                return this;
            }
            @Override public boolean isContentForTagData(String normalName) {
                return false;
            }
            @Override public String defaultNamespace() {
                return "";
            }
            @Override public void initialiseParse(Reader reader, String baseUri, Parser parser) {
            }
            @Override public void process(Token token) {
                // No operation
            }
        };
        Parser parser = new Parser(stubBuilder);
        String html = "<p>Text</p>";
        String baseUri = "http://example.com";
        Document result = parser.parseInput(html, baseUri);
        assertSame(sentinel, result, "Expected stub TreeBuilder.parse return value");
    }

    @Test
    @DisplayName("TC02_O1: parseInput(String, String) with empty HTML string still delegates and returns Document")
    public void test_TC02_O1() {
        Document sentinel = Document.createShell("urn:tc02");
        TreeBuilder stubBuilder = new TreeBuilder() {
            @Override public Document parse(Reader input, String baseUri, Parser parser) {
                return sentinel;
            }
            @Override public List<Node> parseFragment(Reader reader, org.jsoup.nodes.Element context, String baseUri, Parser parser) {
                throw new UnsupportedOperationException();
            }
            @Override public ParseSettings defaultSettings() {
                return new ParseSettings(false, false);
            }
            @Override public TreeBuilder newInstance() {
                return this;
            }
            @Override public boolean isContentForTagData(String normalName) { return false; }
            @Override public String defaultNamespace() { return ""; }
            @Override public void initialiseParse(Reader reader, String baseUri, Parser parser) { }
            @Override public void process(Token token) { /* No operation */ }
        };
        Parser parser = new Parser(stubBuilder);
        String html = "";
        String baseUri = "http://example.com";
        Document result = parser.parseInput(html, baseUri);
        assertSame(sentinel, result, "Expected stub TreeBuilder.parse return value for empty HTML");
    }

    @Test
    @DisplayName("TC03_O1: parseInput(String, String) with null HTML throws NullPointerException at StringReader constructor")
    public void test_TC03_O1() {
        Parser parser = Parser.htmlParser();
        String html = null;
        String baseUri = "http://example.com";
        assertThrows(NullPointerException.class, () -> parser.parseInput(html, baseUri));
    }

    @Test
    @DisplayName("TC04_O2: parseInput(Reader, String) with valid Reader and baseUri delegates to TreeBuilder.parse and returns Document")
    public void test_TC04_O2() {
        Document sentinel = Document.createShell("urn:tc04");
        TreeBuilder stubBuilder = new TreeBuilder() {
            @Override public Document parse(Reader input, String baseUri, Parser parser) {
                return sentinel;
            }
            @Override public List<Node> parseFragment(Reader reader, org.jsoup.nodes.Element context, String baseUri, Parser parser) {
                throw new UnsupportedOperationException();
            }
            @Override public ParseSettings defaultSettings() { return new ParseSettings(false, false); }
            @Override public TreeBuilder newInstance() { return this; }
            @Override public boolean isContentForTagData(String normalName) { return false; }
            @Override public String defaultNamespace() { return ""; }
            @Override public void initialiseParse(Reader reader, String baseUri, Parser parser) { }
            @Override public void process(Token token) { /* No operation */ }
        };
        Parser parser = new Parser(stubBuilder);
        Reader reader = new StringReader("<div>Hi</div>");
        String baseUri = "http://example.com";
        Document result = parser.parseInput(reader, baseUri);
        assertSame(sentinel, result, "Expected stub TreeBuilder.parse return value for Reader input");
    }

    @Test
    @DisplayName("TC05_O2: parseInput(Reader, String) with null Reader triggers stub TreeBuilder.parse to throw IllegalArgumentException")
    public void test_TC05_O2() {
        TreeBuilder stubBuilder = new TreeBuilder() {
            @Override public Document parse(Reader input, String baseUri, Parser parser) {
                if (input == null) throw new IllegalArgumentException("input reader is null");
                return Document.createShell("unused");
            }
            @Override public List<Node> parseFragment(Reader reader, org.jsoup.nodes.Element context, String baseUri, Parser parser) {
                throw new UnsupportedOperationException();
            }
            @Override public ParseSettings defaultSettings() { return new ParseSettings(false, false); }
            @Override public TreeBuilder newInstance() { return this; }
            @Override public boolean isContentForTagData(String normalName) { return false; }
            @Override public String defaultNamespace() { return ""; }
            @Override public void initialiseParse(Reader reader, String baseUri, Parser parser) { }
            @Override public void process(Token token) { /* No operation */ }
        };
        Parser parser = new Parser(stubBuilder);
        Reader reader = null;
        String baseUri = "http://example.com";
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> parser.parseInput(reader, baseUri));
        assertEquals("input reader is null", ex.getMessage());
    }

    @Test
    @DisplayName("TC06_O2: parseInput(Reader, String) with Reader containing empty content still delegates and returns Document")
    public void test_TC06_O2() {
        Document sentinel = Document.createShell("urn:tc06");
        TreeBuilder stubBuilder = new TreeBuilder() {
            @Override public Document parse(Reader input, String baseUri, Parser parser) {
                return sentinel;
            }
            @Override public List<Node> parseFragment(Reader reader, org.jsoup.nodes.Element context, String baseUri, Parser parser) {
                throw new UnsupportedOperationException();
            }
            @Override public ParseSettings defaultSettings() { return new ParseSettings(false, false); }
            @Override public TreeBuilder newInstance() { return this; }
            @Override public boolean isContentForTagData(String normalName) { return false; }
            @Override public String defaultNamespace() { return ""; }
            @Override public void initialiseParse(Reader reader, String baseUri, Parser parser) { }
            @Override public void process(Token token) { /* No operation */ }
        };
        Parser parser = new Parser(stubBuilder);
        Reader reader = new StringReader("");
        String baseUri = "http://example.com";
        Document result = parser.parseInput(reader, baseUri);
        assertSame(sentinel, result, "Expected stub TreeBuilder.parse return value for empty Reader content");
    }
}