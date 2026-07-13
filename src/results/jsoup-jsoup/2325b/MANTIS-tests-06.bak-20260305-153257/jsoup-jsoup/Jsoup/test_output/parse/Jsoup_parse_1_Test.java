package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("parse(File, null charsetName, baseUri) loads HTML from disk and sets correct baseUri")
    public void test_TC08() throws IOException {
        // GIVEN a temporary file containing simple HTML
        File temp = File.createTempFile("test08", ".html");
        temp.deleteOnExit();
        try (FileWriter w = new FileWriter(temp)) {
            w.write("<p>FileTest</p>");
        }
        String charset = null;
        String baseUri = "https://base.example/";
        // WHEN
        Document doc = Jsoup.parse(temp, charset, baseUri);
        // THEN the body HTML must contain the fragment and baseUri set as provided
        assertTrue(doc.body().html().contains("FileTest"),
                "Expected body to contain 'FileTest'");
        assertEquals(baseUri, doc.baseUri(),
                "Expected baseUri to be the provided URI");
    }

    @Test
    @DisplayName("parse(File, invalid charsetName) throws IOException for unsupported charset")
    public void test_TC09() throws IOException {
        // GIVEN a temp file with valid HTML but invalid charset name
        File temp = File.createTempFile("test09", ".html");
        temp.deleteOnExit();
        try (FileWriter w = new FileWriter(temp)) {
            w.write("<p>x</p>");
        }
        String badCharset = "INVALID-CHARSET";
        String baseUri = "";
        // WHEN / THEN should throw IOException due to unsupported charset
        assertThrows(IOException.class, () -> Jsoup.parse(temp, badCharset, baseUri));
    }

    @Test
    @DisplayName("parse(File) uses BOM or meta to default charset and baseUri from file path")
    public void test_TC10() throws IOException {
        // GIVEN a file with simple HTML and default overload
        File temp = File.createTempFile("test10", ".html");
        temp.deleteOnExit();
        try (FileWriter w = new FileWriter(temp)) {
            w.write("<div>ABC</div>");
        }
        // WHEN
        Document doc = Jsoup.parse(temp);
        // THEN it should parse content and baseUri equals file path
        assertTrue(doc.body().html().contains("ABC"),
                "Expected body to contain 'ABC'");
        assertEquals(temp.getAbsolutePath(), doc.baseUri(),
                "Expected baseUri to default to file absolute path");
    }

    @Test
    @DisplayName("parse(File, charsetName) uses provided charset and baseUri from file path")
    public void test_TC11() throws IOException {
        // GIVEN a UTF-8 encoded file with a span element
        File temp = File.createTempFile("test11", ".html");
        temp.deleteOnExit();
        try (FileWriter w = new FileWriter(temp)) {
            w.write("<span>XYZ</span>");
        }
        String charset = "UTF-8";
        // WHEN
        Document doc = Jsoup.parse(temp, charset);
        // THEN select should find exactly one span and baseUri equals file path
        assertEquals(1, doc.select("span").size(),
                "Expected exactly one <span> element");
        assertEquals(temp.getAbsolutePath(), doc.baseUri(),
                "Expected baseUri to default to file absolute path");
    }

    @Test
    @DisplayName("parse(URL, timeout) with non-http protocol throws MalformedURLException")
    public void test_TC12() throws IOException {
        // GIVEN a URL with unsupported 'ftp' protocol
        URL ftpUrl = new URL("ftp://example.com");
        int timeout = 1000;
        // WHEN / THEN MalformedURLException expected due to protocol check
        assertThrows(MalformedURLException.class, () -> Jsoup.parse(ftpUrl, timeout));
    }

    @Test
    @DisplayName("parse(URL, timeout) with very low timeout triggers SocketTimeoutException")
    public void test_TC13() throws Exception {
        // GIVEN an HTTP server that delays response beyond timeout
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                try {
                    Thread.sleep(500); // delay longer than timeout
                    String resp = "<html><body>SLOW</body></html>";
                    exchange.sendResponseHeaders(200, resp.length());
                    exchange.getResponseBody().write(resp.getBytes());
                } catch (InterruptedException e) {
                    // ignore
                } finally {
                    exchange.close();
                }
            }
        });
        server.start();
        int port = server.getAddress().getPort();
        URL url = new URL("http://localhost:" + port + "/");
        int timeout = 100; // low timeout to force failure
        // WHEN / THEN expect SocketTimeoutException
        assertThrows(SocketTimeoutException.class, () -> Jsoup.parse(url, timeout));
        server.stop(0);
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri, parser) uses provided parser to parse stream")
    public void test_TC14() throws IOException {
        // GIVEN an InputStream with an H2 element and custom XML parser
        String content = "<h2>Stream</h2>";
        ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes("UTF-8"));
        String charset = "UTF-8";
        String baseUri = "http://s/";
        Parser xmlParser = Parser.xmlParser();
        // WHEN
        Document doc = Jsoup.parse(in, charset, baseUri, xmlParser);
        // THEN custom parser should pick up H2 text and baseUri set correctly
        assertEquals("Stream", doc.select("h2").first().text(),
                "Expected parser to extract 'Stream' from <h2>");
        assertEquals(baseUri, doc.baseUri(),
                "Expected baseUri to be the provided URI");
    }

    @Test
    @DisplayName("parse(String html, Parser) with null parser throws IllegalArgumentException")
    public void test_TC15() {
        // GIVEN valid HTML but null parser
        String html = "<p>Test</p>";
        Parser nullParser = null;
        // WHEN / THEN IllegalArgumentException per contract for null parser
        assertThrows(IllegalArgumentException.class, () -> Jsoup.parse(html, nullParser));
    }
}