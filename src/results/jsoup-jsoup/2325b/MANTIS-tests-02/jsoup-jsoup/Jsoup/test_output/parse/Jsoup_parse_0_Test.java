package org.jsoup;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.io.IOException;

public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("parse(String, String) with non-empty html and non-empty baseUri returns a Document with correct baseUri")
    public void test_TC01_O1() {
        String html = "<p>Hello</p>";
        String baseUri = "http://example.com/";
        Document doc = Jsoup.parse(html, baseUri);
        assertEquals("http://example.com/", doc.baseUri(), "Base URI should be preserved");
        assertTrue(doc.body().html().contains("<p>Hello</p>"), "Body HTML should contain the original paragraph");
    }

    @Test
    @DisplayName("parse(String, String) with empty html yields empty body element and baseUri preserved")
    public void test_TC02_O1() {
        String html = "";
        String baseUri = "http://u/";
        Document doc = Jsoup.parse(html, baseUri);
        assertEquals("", doc.body().html(), "Empty HTML input should result in empty body");
        assertEquals("http://u/", doc.baseUri(), "Base URI should be preserved even when HTML is empty");
    }

    @Test
    @DisplayName("parse(String, String) with null html throws IllegalArgumentException")
    public void test_TC03_O1() {
        String html = null;
        String baseUri = "http://a/";
        assertThrows(IllegalArgumentException.class, () -> {
            Jsoup.parse(html, baseUri);
        }, "Parsing null HTML should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("parse(String, String) with null baseUri throws IllegalArgumentException")
    public void test_TC04_O1() {
        String html = "<p>X</p>";
        String baseUri = null;
        assertThrows(IllegalArgumentException.class, () -> {
            Jsoup.parse(html, baseUri);
        }, "Parsing with null baseUri should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("parse(String, Parser) with custom parser processes html with empty baseUri")
    public void test_TC05_O2() {
        String html = "any";
        Parser stub = Parser.htmlParser(); // Changed to use valid constructor
        Document doc = Jsoup.parse(html, stub);
        assertEquals("stub", doc.body().html(), "Custom parser stub should define the body HTML");
    }

    @Test
    @DisplayName("parse(String, Parser) with null parser throws IllegalArgumentException")
    public void test_TC06_O2() {
        String html = "<p>hi</p>";
        Parser parser = null;
        assertThrows(IllegalArgumentException.class, () -> {
            Jsoup.parse(html, parser);
        }, "Null parser should cause IllegalArgumentException");
    }

    @Test
    @DisplayName("parse(String, String, Parser) with valid args returns Document from parser.parseInput")
    public void test_TC07_O3() {
        String html = "<div/>";
        String baseUri = "u";
        Parser stub = Parser.htmlParser(); // Changed to use valid constructor
        Document doc = Jsoup.parse(html, baseUri, stub);
        assertEquals("X", doc.body().html(), "Custom parser stub should produce body HTML 'X'");
    }

    @Test
    @DisplayName("parse(String, String, Parser) with null html throws IllegalArgumentException")
    public void test_TC08_O3() {
        String html = null;
        String baseUri = "u";
        Parser parser = Parser.htmlParser();
        assertThrows(IllegalArgumentException.class, () -> {
            Jsoup.parse(html, baseUri, parser);
        }, "Null HTML input should throw IllegalArgumentException even when parser is provided");
    }

    @Test
    @DisplayName("parse(String) with non-empty html yields Document with empty baseUri")
    public void test_TC09_O4() {
        String html = "<span>hi</span>";
        Document doc = Jsoup.parse(html);
        assertEquals("", doc.baseUri(), "Default baseUri for single-arg parse should be empty string");
        assertTrue(doc.body().html().contains("<span>hi</span>"), "Body should contain the parsed span element");
    }

    @Test
    @DisplayName("parse(String, String) with html containing relative links and empty baseUri throws IllegalArgumentException on resolution")
    public void test_TC10_O1() {
        String html = "<a href=\"p.html\">link</a>";
        String baseUri = "";
        assertThrows(IllegalArgumentException.class, () -> {
            Jsoup.parse(html, baseUri);
        }, "Relative link without baseUri should cause IllegalArgumentException due to unresolved URL");
    }
}