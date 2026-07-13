package org.jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("parse(String html, String baseUri) returns Document with body content when html and baseUri are non-null and non-empty")
    public void test_TC01_O1() {
        // Given non-null, non-empty HTML and baseUri to traverse normal parse branch
        String html = "<p>Hello</p>";
        String baseUri = "http://example.com/";
        // When
        Document doc = Jsoup.parse(html, baseUri);
        // Then: body html matches input fragment and baseUri preserved
        assertEquals("<p>Hello</p>", doc.body().html());
        assertEquals("http://example.com/", doc.baseUri());
    }

    @Test
    @DisplayName("parse(String html, String baseUri) returns empty Document body when html is empty string")
    public void test_TC02_O1() {
        // Given empty HTML and non-null baseUri to traverse normal parse branch with no content
        String html = "";
        String baseUri = "base";
        // When
        Document doc = Jsoup.parse(html, baseUri);
        // Then: empty body html and baseUri preserved
        assertEquals("", doc.body().html());
        assertEquals("base", doc.baseUri());
    }

    @Test
    @DisplayName("parse(String html, String baseUri) throws NullPointerException when html is null")
    public void test_TC03_O1() {
        // Given null HTML to trigger NPE before parsing
        String html = null;
        String baseUri = "u";
        // When/Then: expect NullPointerException
        assertThrows(NullPointerException.class, () -> Jsoup.parse(html, baseUri));
    }

    @Test
    @DisplayName("parse(String html, String baseUri) throws NullPointerException when baseUri is null but html non-null")
    public void test_TC04_O1() {
        // Given non-null HTML and null baseUri to trigger NPE on baseUri
        String html = "<div/>";
        String baseUri = null;
        // When/Then: expect NullPointerException
        assertThrows(NullPointerException.class, () -> Jsoup.parse(html, baseUri));
    }

    @Test
    @DisplayName("parse(String html, Parser parser) uses parser.parseInput with empty baseUri")
    public void test_TC05_O2() {
        // Given HTML fragment and a valid parser to traverse parse(String, Parser) branch
        String html = "<span>Hi</span>";
        Parser parser = Parser.htmlParser();
        // When: empty baseUri is used internally
        Document doc = Jsoup.parse(html, parser);
        // Then: body html equals passed HTML fragment
        assertEquals("<span>Hi</span>", doc.body().html());
    }

    @Test
    @DisplayName("parse(String html, Parser parser) throws NullPointerException when parser is null")
    public void test_TC06_O2() {
        // Given non-null HTML and null parser to trigger NPE on parser
        String html = "<a/>";
        Parser parser = null;
        // When/Then: expect NullPointerException
        assertThrows(NullPointerException.class, () -> Jsoup.parse(html, parser));
    }

    @Test
    @DisplayName("parse(String html, String baseUri, Parser parser) uses parser.parseInput and returns Document")
    public void test_TC07_O3() {
        // Given HTML, baseUri, and valid parser to traverse three-arg parse branch
        String html = "<b>Bold</b>";
        String baseUri = "u";
        Parser parser = Parser.htmlParser();
        // When
        Document doc = Jsoup.parse(html, baseUri, parser);
        // Then: fragment parsed via parser and baseUri preserved
        assertEquals("<b>Bold</b>", doc.body().html());
        assertEquals("u", doc.baseUri());
    }

    @Test
    @DisplayName("parse(String html, String baseUri, Parser parser) throws NullPointerException when parser is null")
    public void test_TC08_O3() {
        // Given HTML and baseUri but null parser to trigger NPE
        String html = "x";
        String baseUri = "y";
        Parser parser = null;
        // When/Then: expect NullPointerException
        assertThrows(NullPointerException.class, () -> Jsoup.parse(html, baseUri, parser));
    }
}