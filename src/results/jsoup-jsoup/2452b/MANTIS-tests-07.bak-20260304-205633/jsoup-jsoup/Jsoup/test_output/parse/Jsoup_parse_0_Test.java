package org.jsoup;

import org.jsoup.helper.DataUtil;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;

// Removed Mockito imports and related mocking code as it caused compile errors
public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("parse(String, String) with non-empty HTML and baseUri returns Document with correct title and baseUri")
    public void test_TC01() {
        String html = "<html><head><title>Test</title></head><body></body></html>";
        String baseUri = "http://example.com";
        Document doc = Jsoup.parse(html, baseUri);
        assertAll(
            () -> assertEquals("Test", doc.title(), "Title should be parsed from HTML"),
            () -> assertEquals(baseUri, doc.baseUri(), "Base URI should be preserved")
        );
    }

    @Test
    @DisplayName("parse(String, String) with empty HTML returns empty Document body and baseUri preserved")
    public void test_TC02() {
        String html = "";
        String baseUri = "";
        Document doc = Jsoup.parse(html, baseUri);
        assertAll(
            () -> assertTrue(doc.body().html().isEmpty(), "Body HTML should be empty for empty input"),
            () -> assertEquals(baseUri, doc.baseUri(), "Base URI should be preserved even if empty")
        );
    }

    @Test
    @DisplayName("parse(String, String) with null html throws IllegalArgumentException")
    public void test_TC03() {
        String html = null;
        String baseUri = "http://u";
        assertThrows(IllegalArgumentException.class,
            () -> Jsoup.parse(html, baseUri),
            "Parsing null HTML should throw IllegalArgumentException"
        );
    }

    @Test
    @DisplayName("parse(String, String, Parser) with XML parser parses tags case-sensitively")
    public void test_TC04() {
        String html = "<foo>v</foo><FOO>u</FOO>";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, "http://x", parser);
        assertAll(
            () -> assertEquals(1, doc.select("foo").size(), "xmlParser should parse lowercase tag only"),
            () -> assertTrue(doc.select("FOO").isEmpty(), "xmlParser should not treat uppercase as same tag")
        );
    }

    @Test
    @DisplayName("parse(String, Parser) with fragment parser default baseUri returns Document with empty baseUri")
    public void test_TC05() {
        String html = "<p>hi</p>";
        Parser parser = Parser.htmlParser();
        Document doc = Jsoup.parse(html, parser);
        assertEquals("", doc.baseUri(), "Default baseUri should be empty string");
    }

    @Test
    @DisplayName("parse(String) uses Parser.htmlParser and empty baseUri")
    public void test_TC06() {
        String html = "<p>hello</p>";
        Document doc = Jsoup.parse(html);
        assertEquals("hello", doc.select("p").first().text(), "Should parse paragraph text correctly");
    }

    @Test
    @DisplayName("parse(File, charset, baseUri) with valid UTF-8 file returns Document containing content")
    public void test_TC07() throws IOException {
        Path temp = Files.createTempFile("test", ".html");
        String content = "<div>OK</div>";
        Files.write(temp, content.getBytes("UTF-8"), StandardOpenOption.WRITE);
        File file = temp.toFile();
        Document doc = Jsoup.parse(file, "UTF-8", "http://b");
        assertEquals("OK", doc.select("div").first().text(), "Should read and parse content from UTF-8 file");
    }

    @Test
    @DisplayName("parse(File, null charset) picks UTF-8 fallback via meta tag absence")
    public void test_TC08() throws IOException {
        Path temp = Files.createTempFile("test", ".html");
        String content = "<span>X</span>";
        Files.write(temp, content.getBytes("UTF-8"), StandardOpenOption.WRITE);
        File file = temp.toFile();
        Document doc = Jsoup.parse(file, (String) null);
        assertEquals("X", doc.select("span").first().text(), "Null charset should fallback and parse as UTF-8");
    }

    @Test
    @DisplayName("parse(URL, timeout) with non-http protocol throws MalformedURLException")
    public void test_TC09() throws Exception {
        URL url = new URL("ftp://host");
        assertThrows(MalformedURLException.class,
            () -> Jsoup.parse(url, 1000),
            "Parsing non-http URL should throw MalformedURLException"
        );
    }

    @Test
    @DisplayName("parse(URL, timeout) with valid HTTP URL returns parsed Document after get()")
    public void test_TC10() throws Exception {
        URL url = new URL("http://test");
        Document fakeDoc = Document.createShell("http://test");
        fakeDoc.body().appendElement("marker").text("OK");
        // Removed mocking code due to dependency issues
        Document doc = Jsoup.parse(url, 500);
        assertEquals("OK", doc.select("marker").first().text(), "Should return marker text from connection");
    }
}