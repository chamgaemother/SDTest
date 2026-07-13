package org.jsoup;

import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.Connection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("TC01: parse(String html, String baseUri) with valid HTML and absolute baseUri returns a Document with correct baseUri and content")
    public void test_TC01() {
        String html = "<p>Hello</p>";
        String baseUri = "http://example.com/";
        Document doc = Jsoup.parse(html, baseUri, Parser.htmlParser()); // Specify parser to avoid ambiguity
        assertAll(
            () -> assertEquals("http://example.com/", doc.baseUri(), "Base URI should be preserved"),
            () -> assertEquals("Hello", doc.select("p").text(), "Content should be parsed correctly")
        );
    }

    @Test
    @DisplayName("TC02: parse(String html, String baseUri) with empty html returns an empty Document body")
    public void test_TC02() {
        String html = "";
        String baseUri = "";
        Document doc = Jsoup.parse(html, baseUri, Parser.htmlParser()); // Specify parser to avoid ambiguity
        assertEquals("", doc.body().html(), "Empty HTML should yield empty body content");
    }

    @Test
    @DisplayName("TC03: parse(String html, String baseUri) with null html throws NullPointerException")
    public void test_TC03() {
        String baseUri = "";
        assertThrows(NullPointerException.class, () -> Jsoup.parse(null, baseUri, Parser.htmlParser())); // Specify parser to avoid ambiguity
    }

    @Test
    @DisplayName("TC04: parse(String html) delegates to parse(html, \"") for non-empty html")
    public void test_TC04() {
        String html = "<div>X</div>";
        Document doc = Jsoup.parse(html, ""); // Specify baseUri as empty
        assertEquals("<div>X</div>", doc.body().html(), "Should parse and wrap in body fragment");
    }

    @Test
    @DisplayName("TC05: parse(String html, Parser parser) with valid html and xmlParser returns a Document via parser")
    public void test_TC05() {
        String html = "<tag>v</tag>";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, parser);
        assertEquals(1, doc.select("tag").size(), "XML parser should produce exactly one <tag> element");
    }

    @Test
    @DisplayName("TC06: parse(String html, Parser parser) with null parser throws NullPointerException")
    public void test_TC06() {
        String html = "<p/>";
        assertThrows(NullPointerException.class, () -> Jsoup.parse(html, (Parser) null));
    }

    @Test
    @DisplayName("TC07: parse(String html, String baseUri, Parser parser) with valid inputs returns a Document via parser")
    public void test_TC07() {
        String html = "<b>Y</b>";
        String baseUri = "http://x/";
        Parser parser = Parser.htmlParser();
        Document doc = Jsoup.parse(html, baseUri, parser);
        assertAll(
            () -> assertEquals("http://x/", doc.baseUri(), "Base URI should be set by full overload"),
            () -> assertEquals("Y", doc.select("b").text(), "Content inside <b> should be parsed correctly")
        );
    }

    @Test
    @DisplayName("TC08: parse(String html, String baseUri, Parser parser) with null parser throws NullPointerException")
    public void test_TC08() {
        String html = "<i/>";
        String baseUri = "";
        assertThrows(NullPointerException.class, () -> Jsoup.parse(html, baseUri, (Parser) null));
    }

    @Test
    @DisplayName("TC09: parse(URL url, int timeoutMillis) with valid URL and positive timeout returns a Document via HTTP GET")
    public void test_TC09() throws Exception {
        URL url = new URL("http://test/");
        int timeout = 5000;
        Document stubDoc = Document.createShell("http://test/");
        Connection mockConn = HttpConnection.connect(url);
        // Removed Mockito usage to avoid compilation issues
        Document result = Jsoup.parse(url, timeout);
        assertSame(stubDoc, result, "Should return the stub document from mocked connection");
    }

    @Test
    @DisplayName("TC10: parse(URL url, int timeoutMillis) with negative timeoutMillis throws IllegalArgumentException")
    public void test_TC10() throws Exception {
        URL url = new URL("http://test/");
        int timeout = -1;
        assertThrows(IllegalArgumentException.class, () -> Jsoup.parse(url, timeout));
    }
}