package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("parse(String html, String baseUri) with non-empty html and valid baseUri returns Document with correct title")
    void test_TC01_O1() {
        String html = "<html><head><title>Test Page</title></head><body/></html>";
        String baseUri = "http://example.com/";
        Document doc = Jsoup.parse(html, baseUri);
        assertEquals("Test Page", doc.title(), "Expected title from HTML head");
        assertEquals(baseUri, doc.baseUri(), "Expected baseUri as given");
    }

    @Test
    @DisplayName("parse(String html, String baseUri) with empty html returns empty Document body and retains baseUri")
    void test_TC02_O1() {
        String html = "";
        String baseUri = "https://site/";
        Document doc = Jsoup.parse(html, baseUri);
        assertEquals("", doc.body().html(), "Empty HTML should result in empty body");
        assertEquals(baseUri, doc.baseUri(), "BaseUri should be retained when HTML is empty");
    }

    @Test
    @DisplayName("parse(String html, String baseUri) with null html throws IllegalArgumentException")
    void test_TC03_O1() {
        String html = null;
        String baseUri = "http://x/";
        assertThrows(IllegalArgumentException.class,
            () -> Jsoup.parse(html, baseUri),
            "Parsing null HTML should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("parse(String html, String baseUri) with null baseUri throws IllegalArgumentException")
    void test_TC04_O1() {
        String html = "<p>hi</p>";
        String baseUri = null;
        assertThrows(IllegalArgumentException.class,
            () -> Jsoup.parse(html, baseUri),
            "Parsing with null baseUri should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("parse(String html, Parser parser) with valid html and custom parser uses parser and returns Document")
    void test_TC05_O2() {
        String html = "<tag>value</tag>";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, parser);
        assertEquals("", doc.baseUri(), "Default baseUri should be empty string");
        assertTrue(doc.select("tag").isEmpty(), "XML parser should not produce HTML tag under body");
    }

    @Test
    @DisplayName("parse(String html, String baseUri) with empty baseUri retains empty and uses parser")
    void test_TC06_O3() {
        String html = "<div/>";
        String baseUri = "";
        Parser parser = Parser.htmlParser();
        Document doc = Jsoup.parse(html, baseUri, parser);
        assertEquals("<div></div>", doc.body().html(), "HTML parser should normalize <div/> to <div></div>");
        assertEquals("", doc.baseUri(), "BaseUri should remain empty when provided as empty");
    }

    @Test
    @DisplayName("parse(String html) with valid html returns Document with baseUri empty")
    void test_TC07_O4() {
        String html = "<span>1</span>";
        Document doc = Jsoup.parse(html);
        assertTrue(doc.body().html().contains("<span>1</span>"), "Body should contain the span element as given");
        assertEquals("", doc.baseUri(), "Default baseUri should be empty string");
    }

    @Test
    @DisplayName("parse(String html, String baseUri) with html containing <base> tag overrides provided baseUri")
    void test_TC08_O1() {
        String html = "<html><head><base href=\"http://override/\"></head><body></body></html>";
        String baseUri = "http://original/";
        Document doc = Jsoup.parse(html, baseUri);
        assertEquals("http://override/", doc.baseUri(), "BaseUri should be overridden by <base> tag in HTML");
    }

    @Test
    @DisplayName("parse(InputStream in, String charsetName, String baseUri) with invalid charsetName throws IOException")
    void test_TC09_O5() {
        InputStream in = new ByteArrayInputStream("<p>test</p>".getBytes());
        String charsetName = "INVALID-CHARSET";
        String baseUri = "";
        assertThrows(IOException.class,
            () -> Jsoup.parse(in, charsetName, baseUri),
            "Invalid charset should cause IOException");
    }

    @Test
    @DisplayName("parse(URL url, int timeoutMillis) with non-HTTP URL throws MalformedURLException")
    void test_TC10_O6() throws Exception {
        URL url = new URL("ftp://example.com");
        int timeout = 1000;
        assertThrows(MalformedURLException.class,
            () -> Jsoup.parse(url, timeout),
            "Non-HTTP URL protocol should throw MalformedURLException");
    }
}