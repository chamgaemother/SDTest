package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("TC01_O1: parse(String html, String baseUri) with valid HTML and non-empty baseUri returns parsed Document")
    public void test_TC01_O1() {
        String html = "<p>Test</p>";
        String baseUri = "http://example.com/";
        Document doc = Jsoup.parse(html, baseUri);
        assertAll(
            () -> assertTrue(doc.html().contains("<p>Test</p>"), "Should contain the paragraph HTML"),
            () -> assertEquals("http://example.com/", doc.baseUri(), "Base URI must be set correctly")
        );
    }

    @Test
    @DisplayName("TC02_O1: parse(String html, String baseUri) with null html throws IllegalArgumentException")
    public void test_TC02_O1() {
        String html = null;
        String baseUri = "http://x/";
        assertThrows(IllegalArgumentException.class, () -> Jsoup.parse(html, baseUri));
    }

    @Test
    @DisplayName("TC03_O2: parse(String html, String baseUri, Parser parser) with valid parser returns Document via parseInput")
    public void test_TC03_O2() {
        String html = "<div>OK</div>";
        String baseUri = "base";
        Parser p = Parser.xmlParser();
        Document doc = Jsoup.parse(html, baseUri, p);
        assertAll(
            () -> assertTrue(doc.html().contains("<div>OK</div>"), "Should include provided div"),
            () -> assertEquals("base", doc.baseUri(), "Should set baseUri from argument")
        );
    }

    @Test
    @DisplayName("TC04_O3: parse(String html, Parser parser) with empty html yields empty Document body")
    public void test_TC04_O3() {
        String html = "";
        Parser p = Parser.htmlParser();
        Document doc = Jsoup.parse(html, p);
        assertAll(
            () -> assertTrue(doc.body().html().isEmpty(), "Body should be empty for empty input"),
            () -> assertEquals("", doc.baseUri(), "Default baseUri is empty string")
        );
    }

    @Test
    @DisplayName("TC05_O4: parse(String html) with fragment HTML returns Document with body containing fragment")
    public void test_TC05_O4() {
        String html = "<span>F</span>";
        Document doc = Jsoup.parse(html);
        assertAll(
            () -> assertEquals("<span>F</span>", doc.body().html(), "Body must reflect the fragment"),
            () -> assertEquals("", doc.baseUri(), "Default baseUri is empty string")
        );
    }

    @Test
    @DisplayName("TC06_O5: parse(File file, String charsetName, String baseUri) with valid UTF-8 file returns Document")
    public void test_TC06_O5() throws IOException {
        Path temp = Files.createTempFile("test", ".html");
        Files.write(temp, "<h1>Hi</h1>".getBytes("UTF-8"));
        File f = temp.toFile();
        String cs = "UTF-8";
        String base = "http://b/";
        Document doc = Jsoup.parse(f, cs, base);
        assertAll(
            () -> assertTrue(doc.html().contains("<h1>Hi</h1>"), "HTML from file must be parsed"),
            () -> assertEquals("http://b/", doc.baseUri(), "BaseUri must match provided")
        );
        Files.delete(temp);
    }

    @Test
    @DisplayName("TC07_O6: parse(File file, String charsetName) with null charsetName defaults to BOM/meta detection")
    public void test_TC07_O6() throws IOException {
        Path temp = Files.createTempFile("a", ".html");
        Files.write(temp, "<p>X</p>".getBytes());
        File f = temp.toFile();
        String cs = null;
        Document doc = Jsoup.parse(f, cs);
        assertAll(
            () -> assertTrue(doc.html().contains("<p>X</p>"), "Should parse HTML even without charset hint"),
            () -> assertEquals(f.getAbsolutePath(), doc.baseUri(), "BaseUri should default to file path")
        );
        Files.delete(temp);
    }

    @Test
    @DisplayName("TC08_O7: parse(InputStream in, String charsetName, String baseUri) with ByteArrayInputStream and custom baseUri")
    public void test_TC08_O7() throws IOException {
        String s = "<b>1</b>";
        InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
        String cs = "UTF-8";
        String base = "http://u/";
        Document doc = Jsoup.parse(in, cs, base);
        assertAll(
            () -> assertEquals("<b>1</b>", doc.body().html(), "Body HTML must equal input"),
            () -> assertEquals("http://u/", doc.baseUri(), "BaseUri must match argument")
        );
    }

    @Test
    @DisplayName("TC09_O8: parse(URL url, int timeoutMillis) with valid http URL returns Document")
    public void test_TC09_O8() throws Exception {
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/test", exch -> {
            byte[] resp = "<i>OK</i>".getBytes("UTF-8");
            exch.sendResponseHeaders(200, resp.length);
            exch.getResponseBody().write(resp);
            exch.close();
        });
        server.start();
        int port = server.getAddress().getPort();
        URL url = new URL("http://localhost:" + port + "/test");
        Document doc = Jsoup.parse(url, 5000);
        server.stop(0);
        assertTrue(doc.html().contains("<i>OK</i>"), "Should fetch and parse <i>OK</i> from server");
    }

    @Test
    @DisplayName("TC10_O1: parse+clean relative-link with preserveRelativeLinks true returns link preserved")
    public void test_TC10_O1() {
        String html = "<a href='/x'>X</a>";
        String base = "";
        Safelist s = Safelist.relaxed().preserveRelativeLinks(true);
        String out = Jsoup.clean(html, base, s);
        assertEquals("<a href=\"/x\">X</a>", out, "Relative link should be preserved by safelist");
    }
}