package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.Connection;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("parse(String html) with non-empty HTML returns a Document containing expected text")
    public void test_TC01_O1() {
        // Input HTML contains a paragraph, should parse and extract text from body
        String html = "<p>Hello World</p>";
        Document doc = Jsoup.parse(html);
        assertEquals("Hello World", doc.body().text(), 
            "Expected body text to be 'Hello World' for simple paragraph HTML");
    }

    @Test
    @DisplayName("parse(String html) with empty string returns an empty Document body")
    public void test_TC02_O1() {
        // Empty HTML results in empty body html
        String html = "";
        Document doc = Jsoup.parse(html);
        assertEquals("", doc.body().html(),
            "Expected empty body html for empty input");
    }

    @Test
    @DisplayName("parse(String html) with null HTML throws IllegalArgumentException")
    public void test_TC03_O1() {
        // Passing null should be considered invalid input
        assertThrows(IllegalArgumentException.class, () -> {
            Jsoup.parse((String) null);
        }, "Expected IllegalArgumentException when html is null");
    }

    @Test
    @DisplayName("parse(String html, String baseUri) resolves relative links against baseUri")
    public void test_TC04_O2() {
        // Relative href '/page' should resolve against baseUri
        String html = "<a href=\"/page\">link</a>";
        String baseUri = "https://example.com";
        Document doc = Jsoup.parse(html, baseUri);
        String absHref = doc.select("a").first().absUrl("href");
        assertEquals("https://example.com/page", absHref,
            "Expected absolute URL to be resolved against provided baseUri");
    }

    @Test
    @DisplayName("parse(String html, Parser parser) with XML parser parses self-closing tags")
    public void test_TC05_O3() {
        // Using XML parser should output self-closing br tag
        String html = "<body><br></body>";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, parser);
        assertTrue(doc.body().html().contains("<br/>"),
            "Expected XML parser to produce self-closing <br/> tag");
    }

    @Test
    @DisplayName("parse(File file, String charsetName, String baseUri) with valid UTF-8 file returns parsed content")
    public void test_TC06_O4() throws IOException {
        // Create temporary file with UTF-8 content and parse it
        Path temp = Files.createTempFile("jsoupTest", ".html");
        Files.write(temp, "<div>Test</div>".getBytes(StandardCharsets.UTF_8));
        File file = temp.toFile();
        String charset = "UTF-8";
        String baseUri = "";
        Document doc = Jsoup.parse(file, charset, baseUri);
        assertEquals("Test", doc.body().text(),
            "Expected parsed text 'Test' from file content");
    }

    @Test
    @DisplayName("parse(File file) auto-detects charset and uses file path as baseUri")
    public void test_TC07_O5() throws IOException {
        // File parse should auto-detect charset (no BOM or meta) and baseUri=file path
        Path temp = Files.createTempFile("jsoupTestAuto", ".html");
        Files.write(temp, "<span>Auto</span>".getBytes(StandardCharsets.UTF_8));
        File file = temp.toFile();
        Document doc = Jsoup.parse(file);
        assertEquals("Auto", doc.body().text(),
            "Expected parsed text 'Auto' from auto-detect file parser");
        assertEquals(file.getAbsolutePath(), doc.baseUri(),
            "Expected baseUri to equal the file's absolute path");
    }

    @Test
    @DisplayName("parse(URL url, int timeoutMillis) with invalid protocol throws MalformedURLException")
    public void test_TC08_O6() throws Exception {
        // FTP protocol is invalid for HTTP connect
        URL url = new URL("ftp://example.com");
        int timeout = 1000;
        assertThrows(MalformedURLException.class, () -> {
            Jsoup.parse(url, timeout);
        }, "Expected MalformedURLException for unsupported protocol");
    }

    @Test
    @DisplayName("parse(URL url, int timeoutMillis) with unreachable host throws SocketTimeoutException")
    public void test_TC09_O6() throws Exception {
        // Non-routable IP with minimal timeout triggers SocketTimeoutException
        URL url = new URL("http://10.255.255.1");
        int timeout = 1;
        assertThrows(SocketTimeoutException.class, () -> {
            Jsoup.parse(url, timeout);
        }, "Expected SocketTimeoutException for unreachable host");
    }
}