package org.jsoup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.helper.DataUtil;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("parse(html non-empty, baseUri non-empty) returns parsed Document with same body")
    public void test_TC01_string_baseUri() {
        String html = "<p>Hello</p>";
        String baseUri = "http://example.com/";
        Document doc = Jsoup.parse(html, baseUri);
        assertEquals("<p>Hello</p>", doc.body().html());
        assertEquals("http://example.com/", doc.baseUri());
    }

    @Test
    @DisplayName("parse(html empty, baseUri empty) returns empty body Document")
    public void test_TC02_string_emptyHtml() {
        String html = "";
        String baseUri = "";
        Document doc = Jsoup.parse(html, baseUri);
        assertEquals("", doc.body().html());
        assertEquals("", doc.baseUri());
    }

    @Test
    @DisplayName("parse(html null, baseUri any) throws IllegalArgumentException for null html input")
    public void test_TC03_string_nullHtml() {
        String html = null;
        String baseUri = "http://example.com/";
        assertThrows(IllegalArgumentException.class, () -> Jsoup.parse(html, baseUri));
    }

    @Test
    @DisplayName("parse(html non-empty, baseUri empty, custom parser) uses provided Parser to parse")
    public void test_TC04_string_baseUri_parser() {
        String html = "irrelevant";
        Parser parser = Parser.htmlParser(); // Changed to use valid constructor
        Document doc = Jsoup.parse(html, parser);
        assertEquals("<x/>", doc.body().html());
    }

    @Test
    @DisplayName("parse(html non-empty, baseUri non-empty, custom parser) delegates to parser.parseInput")
    public void test_TC05_string_html_baseUri_parser() {
        String html = "body";
        String baseUri = "https://a/b";
        Parser parser = Parser.htmlParser(); // Changed to use valid constructor
        Document doc = Jsoup.parse(html, baseUri, parser);
        assertEquals("https://a/b", doc.baseUri());
        assertEquals("body", doc.body().html());
    }

    @Test
    @DisplayName("parse(File exists, charset valid, baseUri) returns Document loaded from file")
    public void test_TC06_file_charset_baseUri() throws IOException {
        File temp = File.createTempFile("jsoup_test", ".html");
        temp.deleteOnExit();
        try (FileWriter w = new FileWriter(temp)) {
            w.write("<p>F</p>");
        }
        String charset = "UTF-8";
        String baseUri = "u";
        Document doc = Jsoup.parse(temp, charset, baseUri);
        assertEquals("<p>F</p>", doc.body().html());
        assertEquals("u", doc.baseUri());
    }

    @Test
    @DisplayName("parse(File non-existent) throws IOException when file missing")
    public void test_TC07_file_notFound() {
        File f = new File("nonexistent.html");
        assertThrows(IOException.class, () -> Jsoup.parse(f));
    }

    @Test
    @DisplayName("parse(URL with unsupported protocol) throws MalformedURLException")
    public void test_TC08_url_timeout() throws Exception {
        URL url = new URL("ftp://example.com");
        int timeout = 1000;
        assertThrows(MalformedURLException.class, () -> Jsoup.parse(url, timeout));
    }

    @Test
    @DisplayName("clean(bodyHtml non-empty, baseUri, safelist) returns cleaned HTML with no disallowed tags")
    public void test_TC09_clean_via_parseBodyFragment() {
        String html = "<b>bold</b>";
        String baseUri = "";
        Safelist safelist = Safelist.none();
        String result = Jsoup.clean(html, baseUri, safelist);
        assertEquals("bold", result);
    }

    @Test
    @DisplayName("isValid returns true when html allowed, false when disallowed")
    public void test_TC10_isValid_trueFalse() {
        Safelist safelist = Safelist.simpleText();
        boolean ok1 = Jsoup.isValid("abc", safelist);
        boolean ok2 = Jsoup.isValid("<img/>", safelist);
        assertAll(
            () -> assertTrue(ok1, "Plain text should be valid"),
            () -> assertFalse(ok2, "IMG tag should not be valid under simpleText safelist")
        );
    }
}