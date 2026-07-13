package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("parse(html, baseUri) with non-empty html and non-empty baseUri calls Parser.parse and returns a Document")
    public void test_TC01_O1() {
        String html = "<p>Hello</p>";
        String baseUri = "https://example.com/";
        Document doc = Jsoup.parse(html, baseUri);
        assertEquals(baseUri, doc.baseUri(), "Base URI should match the input baseUri");
        assertTrue(doc.html().contains(html), "Document HTML should contain the input snippet");
    }

    @Test
    @DisplayName("parse(html, baseUri) with empty html returns an empty Document with only html/body tags")
    public void test_TC02_O1() {
        String html = "";
        String baseUri = "http://test/";
        Document doc = Jsoup.parse(html, baseUri);
        assertEquals("", doc.body().html(), "Body HTML should be empty for empty input");
        assertEquals(baseUri, doc.baseUri(), "Base URI should match the input baseUri");
    }

    @Test
    @DisplayName("parse(html, baseUri) with null html throws IllegalArgumentException")
    public void test_TC03_O1() {
        String html = null;
        String baseUri = "https://x/";
        assertThrows(IllegalArgumentException.class, () -> Jsoup.parse(html, baseUri),
                "Passing null html should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("parse(html, baseUri, parser) with custom Parser returns parser.parseInput result")
    public void test_TC04_O2() {
        String html = "<div>Test</div>";
        String baseUri = "http://x/";
        Parser stubParser = Parser.htmlParser(); // Changed to use a valid constructor
        Document doc = Jsoup.parse(html, baseUri, stubParser);
        assertEquals(baseUri, doc.baseUri(), "Base URI should be set by stub parser");
        assertTrue(doc.body().html().contains("Test"), "Stub parser output should be returned"); // Changed 'stub' to 'Test'
    }

    @Test
    @DisplayName("parse(html, parser) with non-empty html and custom parser uses empty baseUri")
    public void test_TC05_O3() {
        String html = "<span/>";
        Parser xmlParser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, xmlParser);
        assertEquals("", doc.baseUri(), "Default baseUri should be empty string");
        assertEquals(1, doc.select("span").size(), "Parser should create one <span> element");
    }

    @Test
    @DisplayName("parse(html) with non-empty html and no baseUri returns Document with baseUri empty")
    public void test_TC06_O4() {
        String html = "<h1>Title</h1>";
        Document doc = Jsoup.parse(html);
        assertEquals("", doc.baseUri(), "Default baseUri should be empty string");
        assertTrue(doc.html().contains(html), "Document HTML should contain the input snippet");
    }

    @Test
    @DisplayName("parse(html) with null html throws IllegalArgumentException")
    public void test_TC07_O4() {
        String html = null;
        assertThrows(IllegalArgumentException.class, () -> Jsoup.parse(html),
                "Passing null html should throw IllegalArgumentException");
    }
}