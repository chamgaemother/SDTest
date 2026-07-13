package org.jsoup;

import org.jsoup.helper.DataUtil;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("TC01_O1: parse(String html, String baseUri) returns a Document with correct baseUri when html is simple and baseUri is non-empty")
    public void test_TC01_O1() {
        // B0→B1→B4: cover public overload with two args, non-empty baseUri
        String html = "<p>Hello</p>";
        String baseUri = "https://example.com/";
        Document doc = Jsoup.parse(html, baseUri);
        assertEquals("https://example.com/", doc.baseUri(), "baseUri should be as provided");
        assertTrue(doc.html().contains("<p>Hello</p>"), "HTML should contain the provided fragment");
    }

    @Test
    @DisplayName("TC02_O1: parse(String html) returns a Document with empty baseUri when no baseUri provided")
    public void test_TC02_O1() {
        // B0→B1→B5: cover single-arg overload, empty baseUri path
        String html = "<div>Test</div>";
        Document doc = Jsoup.parse(html);
        assertEquals("", doc.baseUri(), "baseUri should be empty string for no-baseUri overload");
        assertTrue(doc.body().html().contains("<div>Test</div>"), "Body HTML should contain the provided div");
    }

    @Test
    @DisplayName("TC03_O1: parse(String html, Parser parser) uses provided parser and empty baseUri")
    public void test_TC03_O1() {
        // B0→B1→B6: cover overload with Parser, uses empty baseUri
        String html = "<span>X</span>";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, parser);
        assertEquals("", doc.baseUri(), "baseUri should be empty when using Parser-only overload");
        assertEquals("<span>X</span>", doc.body().html(), "Body HTML should match parser output");
    }

    @Test
    @DisplayName("TC04_O1: parse(String html, String baseUri, Parser parser) uses provided parser and baseUri")
    public void test_TC04_O1() {
        // B0→B1→B7: cover three-arg overload
        String html = "<a href=\"b\">c</a>";
        String baseUri = "http://a/";
        Parser parser = Parser.htmlParser();
        Document doc = Jsoup.parse(html, baseUri, parser);
        assertEquals("http://a/", doc.baseUri(), "baseUri should be as provided for three-arg overload");
        assertTrue(doc.body().html().contains("<a href=\"b\">c</a>"), "Body HTML should contain the provided link");
    }

    @Test
    @DisplayName("TC05_O2: parse(File file, String charsetName, String baseUri) returns Document when file exists and charsetName is null")
    public void test_TC05_O2() throws IOException {
        // B0→B2→B8: cover File load with null charset
        Path tmp = Files.createTempFile("jsoup-test", ".html");
        Files.write(tmp, "<h1>Hdr</h1>".getBytes("UTF-8"));
        File tempFile = tmp.toFile();
        String charsetName = null;
        String baseUri = tempFile.getAbsolutePath();
        Document doc = Jsoup.parse(tempFile, charsetName, baseUri);
        assertEquals(baseUri, doc.baseUri(), "baseUri should equal file path when provided");
        assertTrue(doc.body().html().contains("<h1>Hdr</h1>"), "Body HTML should contain header from file");
        tempFile.delete();
    }

    @Test
    @DisplayName("TC06_O2: parse(File file, String charsetName) throws IOException when charsetName is invalid")
    public void test_TC06_O2() throws IOException {
        // B0→B2→B9: invalid charsetName should trigger IOException
        Path tmp = Files.createTempFile("jsoup-test-invalid", ".html");
        Files.write(tmp, "x".getBytes("UTF-8"));
        File tempFile = tmp.toFile();
        String invalidCharset = "INVALID-CHARSET";
        assertThrows(IOException.class, () -> Jsoup.parse(tempFile, invalidCharset),
            "Invalid charset should throw IOException");
        tempFile.delete();
    }

    @Test
    @DisplayName("TC07_O3: parse(Path path, String charsetName, String baseUri) returns Document for valid path and charset")
    public void test_TC07_O3() throws IOException {
        // B0→B3→B10: cover Path overload with charset and baseUri
        Path tmp = Files.createTempFile("jsoup-test-path", ".html");
        Files.write(tmp, "<b>bold</b>".getBytes("UTF-8"));
        String charsetName = "UTF-8";
        String baseUri = "http://test/";
        Document doc = Jsoup.parse(tmp, charsetName, baseUri);
        assertEquals(baseUri, doc.baseUri(), "baseUri should be as provided");
        assertTrue(doc.body().html().contains("<b>bold</b>"), "Body HTML should contain bold tag");
        Files.delete(tmp);
    }

    @Test
    @DisplayName("TC08_O4: parse(InputStream in, String charsetName, String baseUri) returns Document and closes stream")
    public void test_TC08_O4() throws IOException {
        // B0→B4→B11: test InputStream overload closes stream
        byte[] data = "<i>it</i>".getBytes("UTF-8");
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        String charsetName = null;
        String baseUri = "";
        Document doc = Jsoup.parse(in, charsetName, baseUri);
        assertEquals("<i>it</i>", doc.body().html(), "Body HTML should match input stream content");
        assertEquals(-1, in.read(), "InputStream should be closed/read to end");
    }

    @Test
    @DisplayName("TC09_O5: parse(URL url, int timeoutMillis) throws MalformedURLException when protocol not http or https")
    public void test_TC09_O5() throws Exception {
        // B0→B5→B12: unsupported protocol should throw MalformedURLException
        URL url = new URL("ftp://example.com");
        int timeout = 1000;
        assertThrows(MalformedURLException.class, () -> Jsoup.parse(url, timeout),
            "Non-http(s) protocol should cause MalformedURLException");
    }

    @Test
    @DisplayName("TC10_O5: parse(URL url, int timeoutMillis) returns Document for valid http URL within timeout")
    public void test_TC10_O5() throws Exception {
        // B0→B5→B13: launch simple HTTP server to serve HTML
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            byte[] resp = "<title>OK</title>".getBytes("UTF-8");
            exchange.sendResponseHeaders(200, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        });
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
        int port = server.getAddress().getPort();
        URL url = new URL("http://localhost:" + port);
        Document doc = Jsoup.parse(url, 2000);
        assertEquals("OK", doc.title(), "Title should be parsed from served HTML");
        server.stop(0);
    }
}