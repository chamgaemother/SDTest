package org.jsoup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("parse(html) invokes Parser.parse with empty baseUri for default overload")
    public void test_TC01_O1() {
        String html = "<p>test</p>";
        Document result = Jsoup.parse(html);
        Document expected = Parser.parse(html, "");
        assertEquals(expected, result, "Expected default-baseUri parse to delegate to Parser.parse with empty baseUri");
    }

    @Test
    @DisplayName("parse(html,baseUri) passes non-empty baseUri through to Parser.parse")
    public void test_TC02_O2() {
        String html = "<a href='x'>x</a>";
        String baseUri = "http://example.com/";
        Document result = Jsoup.parse(html, baseUri);
        Document expected = Parser.parse(html, baseUri);
        assertEquals(expected, result, "Expected non-empty baseUri parse to delegate to Parser.parse with given baseUri");
    }

    @Test
    @DisplayName("parse(html,baseUri,parser) uses provided Parser to parse")
    public void test_TC03_O3() {
        String html = "<xml/>";
        String baseUri = "http://x/";
        Parser parser = Parser.xmlParser();
        Document result = Jsoup.parse(html, baseUri, parser);
        Document expected = parser.parseInput(html, baseUri);
        assertEquals(expected, result, "Expected custom parser overload to use parser.parseInput");
    }

    @Test
    @DisplayName("parse(html,parser) uses provided Parser with empty baseUri")
    public void test_TC04_O4() {
        String html = "<tag/>";
        Parser parser = Parser.htmlParser();
        Document result = Jsoup.parse(html, parser);
        Document expected = parser.parseInput(html, "");
        assertEquals(expected, result, "Expected parser overload to use empty baseUri and parser.parseInput");
    }

    @Test
    @DisplayName("parse(null) throws NullPointerException when html is null")
    public void test_TC05_O5() {
        String html = null;
        assertThrows(NullPointerException.class, () -> Jsoup.parse(html),
            "Expected parse(null) to throw NullPointerException");
    }

    @Test
    @DisplayName("parse(html,baseUri) throws NullPointerException when baseUri is null")
    public void test_TC06_O6() {
        String html = "<p>ok</p>";
        String baseUri = null;
        assertThrows(NullPointerException.class, () -> Jsoup.parse(html, baseUri),
            "Expected parse(html, null) to throw NullPointerException");
    }

    @Test
    @DisplayName("parse(html,baseUri,parser) throws IllegalArgumentException when parser is null")
    public void test_TC07_O7() {
        String html = "<p>ok</p>";
        String baseUri = "";
        Parser parser = null;
        assertThrows(IllegalArgumentException.class, () -> Jsoup.parse(html, baseUri, parser),
            "Expected parse(html, baseUri, null) to throw IllegalArgumentException");
    }

    @Test
    @DisplayName("parse(html,parser) throws IllegalArgumentException when parser is null")
    public void test_TC08_O8() {
        String html = "<p>ok</p>";
        Parser parser = null;
        assertThrows(IllegalArgumentException.class, () -> Jsoup.parse(html, parser),
            "Expected parse(html, null parser) to throw IllegalArgumentException");
    }

    @Test
    @DisplayName("parse(html,baseUri) resolves relative links using baseUri")
    public void test_TC09_O9() {
        String html = "<a href='/rel'>link</a>";
        String baseUri = "http://x.com";
        Document doc = Jsoup.parse(html, baseUri);
        Element link = doc.select("a").first();
        assertEquals("http://x.com/rel", link.absUrl("href"),
            "Expected relative href to resolve against provided baseUri");
    }

    @Test
    @DisplayName("parse(html) retains relative href as empty resolution when baseUri is empty")
    public void test_TC10_O10() {
        String html = "<a href='/rel'>link</a>";
        Document doc = Jsoup.parse(html);
        Element link = doc.select("a").first();
        assertEquals("", link.absUrl("href"),
            "Expected absUrl to be empty string when no baseUri provided");
    }
}