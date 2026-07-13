package org.jsoup;

import com.sun.net.httpserver.HttpServer;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("parse(String html, String baseUri) with non-empty html and baseUri returns a Document with correct baseUri")
    public void test_TC01_O1() {
        String html = "<p>Test</p>";
        String baseUri = "http://example.com";
        Document doc = Jsoup.parse(html, baseUri);
        assertEquals("http://example.com", doc.baseUri(), "Expected baseUri to match input baseUri");
        assertTrue(doc.html().contains("<p>Test</p>"), "Expected document HTML to contain the input fragment");
    }

    @Test
    @DisplayName("parse(String html, String baseUri, Parser parser) uses provided parser and returns parsed Document")
    public void test_TC02_O2() {
        String html = "<tag/>";
        String baseUri = "";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, baseUri, parser);
        assertEquals("", doc.baseUri(), "Expected baseUri to remain empty when provided empty string");
        assertEquals(1, doc.select("tag").size(), "Expected one <tag> element parsed by xmlParser");
    }

    @Test
    @DisplayName("parse(String html, Parser parser) with empty baseUri branch executes parser.parseInput with empty baseUri")
    public void test_TC03_O3() {
        String html = "<div>1</div>";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, parser);
        assertEquals("", doc.baseUri(), "Expected baseUri to be empty string when using parse(html, parser)");
    }

    @Test
    @DisplayName("parse(String html) delegates to Parser.parse(html, \"") and returns empty-baseUri Document")
    public void test_TC04_O4() {
        String html = "<span>abc</span>";
        Document doc = Jsoup.parse(html);
        assertEquals("", doc.baseUri(), "Expected baseUri to be empty for parse(html)");
        assertTrue(doc.body().html().contains(html), "Expected body HTML to contain the original html fragment");
    }

    @Test
    @DisplayName("parse(File file, String charsetName, String baseUri) with valid file and charset returns Document")
    public void test_TC05_O5() throws IOException {
        File temp = File.createTempFile("jsoupTest", ".html");
        temp.deleteOnExit();
        String content = "<p>F</p>";
        Files.write(temp.toPath(), content.getBytes(StandardCharsets.UTF_8));
        String charsetName = "UTF-8";
        String baseUri = "http://base/";
        Document doc = Jsoup.parse(temp, charsetName, baseUri);
        assertEquals(baseUri, doc.baseUri(), "Expected baseUri to match provided baseUri for file parse");
        assertTrue(doc.html().contains(content), "Expected document HTML to contain file content");
    }

    @Test
    @DisplayName("parse(File file, String charsetName) calls DataUtil.load with file.getAbsolutePath() as baseUri")
    public void test_TC06_O6() throws IOException {
        File temp = File.createTempFile("jsoupTest", ".html");
        temp.deleteOnExit();
        String content = "<p>X</p>";
        Files.write(temp.toPath(), content.getBytes(StandardCharsets.UTF_8));
        String charsetName = null;
        Document doc = Jsoup.parse(temp, charsetName);
        assertEquals(temp.getAbsolutePath(), doc.baseUri(), "Expected baseUri to be file absolute path when charsetName is null");
        assertTrue(doc.html().contains(content), "Expected document HTML to contain file content");
    }

    @Test
    @DisplayName("parse(File file) uses null charset and file AbsolutePath as baseUri")
    public void test_TC07_O7() throws IOException {
        File temp = File.createTempFile("jsoupTest", ".html");
        temp.deleteOnExit();
        String content = "<div>Y</div>";
        Files.write(temp.toPath(), content.getBytes(StandardCharsets.UTF_8));
        Document doc = Jsoup.parse(temp);
        assertEquals(temp.getAbsolutePath(), doc.baseUri(), "Expected baseUri to be file absolute path for file-only parse");
        assertTrue(doc.html().contains(content), "Expected document HTML to contain file content");
    }

    @Test
    @DisplayName("parse(URL url, int timeoutMillis) with valid HTTP URL and zero timeout calls con.get() successfully")
    public void test_TC08_O8() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            String response = "<h1>ok</h1>";
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
        try {
            int port = server.getAddress().getPort();
            URL url = new URL("http://localhost:" + port + "/");
            int timeoutMillis = 0;
            Document doc = Jsoup.parse(url, timeoutMillis);
            assertTrue(doc.body().html().contains("<h1>ok</h1>"), "Expected body HTML to contain <h1>ok</h1> from HTTP server");
        } finally {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("parse(URL url, int timeoutMillis) with invalid protocol throws MalformedURLException")
    public void test_TC09_O8() throws Exception {
        URL url = new URL("ftp://example.com");
        int timeoutMillis = 1000;
        assertThrows(MalformedURLException.class, () -> Jsoup.parse(url, timeoutMillis),
            "Expected MalformedURLException for non-http(s) URL protocol");
    }
}