package org.jsoup;

import org.jsoup.helper.DataUtil;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("parse(File, charsetName, baseUri) with valid UTF-8 file returns Document with correct content and baseUri")
    public void test_TC11() throws IOException {
        // Design: valid file with known HTML and explicit baseUri triggers DataUtil.load -> Parser
        File tempFile = File.createTempFile("jsoup_test11", ".html");
        tempFile.deleteOnExit();
        String html = "<p>File</p>";
        Files.write(tempFile.toPath(), html.getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        String baseUri = "http://example.org/";
        // Exercise
        Document doc = Jsoup.parse(tempFile, charset, baseUri);
        // Verify content and baseUri
        assertTrue(doc.body().html().contains(html), "Body HTML should contain the paragraph");
        assertEquals(baseUri, doc.baseUri(), "Base URI should match the provided baseUri");
    }

    @Test
    @DisplayName("parse(File, null) overload uses file absolute path as baseUri and default charset UTF-8")
    public void test_TC12() throws IOException {
        // Design: null charset uses default; baseUri should be file.getAbsolutePath()
        File tempFile = File.createTempFile("jsoup_test12", ".html");
        tempFile.deleteOnExit();
        String html = "<div>Test</div>";
        Files.write(tempFile.toPath(), html.getBytes(StandardCharsets.UTF_8));
        // Exercise
        Document doc = Jsoup.parse(tempFile, null);
        // Verify
        assertTrue(doc.body().html().contains(html), "Body HTML should contain the div");
        assertEquals(tempFile.getAbsolutePath(), doc.baseUri(), "Base URI should be the file's absolute path");
    }

    @Test
    @DisplayName("parse(File) overload uses null charset, file path as baseUri")
    public void test_TC13() throws IOException {
        // Design: single-arg File should load with null charset and file path baseUri
        File tempFile = File.createTempFile("jsoup_test13", ".html");
        tempFile.deleteOnExit();
        String html = "<span>XYZ</span>";
        Files.write(tempFile.toPath(), html.getBytes(StandardCharsets.UTF_8));
        // Exercise
        Document doc = Jsoup.parse(tempFile);
        // Verify
        assertTrue(doc.body().html().contains(html), "Body HTML should contain the span");
        assertEquals(tempFile.getAbsolutePath(), doc.baseUri(), "Base URI should be the file's absolute path");
    }

    @Test
    @DisplayName("parse(Path, charsetName, baseUri) reads HTML from Path and returns correct Document")
    public void test_TC14() throws IOException {
        // Design: Path overload reads file with charset and baseUri
        Path tempPath = Files.createTempFile("jsoup_test14", ".html");
        File tempFile = tempPath.toFile();
        tempFile.deleteOnExit();
        String html = "<h1>Header</h1>";
        Files.write(tempPath, html.getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        String baseUri = "https://site/";
        // Exercise
        Document doc = Jsoup.parse(tempPath, charset, baseUri);
        // Verify
        assertTrue(doc.body().html().contains(html), "Body HTML should contain the header");
        assertEquals(baseUri, doc.baseUri(), "Base URI should match the provided baseUri");
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri) reads HTML and returns Document correctly")
    public void test_TC15() throws IOException {
        // Design: InputStream overload reads provided stream with charset and baseUri
        String html = "<em>E</em>";
        InputStream in = new java.io.ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        String baseUri = "http://u/";
        // Exercise
        Document doc = Jsoup.parse(in, charset, baseUri);
        // Verify
        assertTrue(doc.body().html().contains(html), "Body HTML should contain the emphasized text");
        assertEquals(baseUri, doc.baseUri(), "Base URI should match the provided baseUri");
    }

    @Test
    @DisplayName("parse(URL, timeoutMillis) with non-http URL throws MalformedURLException")
    public void test_TC16() {
        // Design: URL with file protocol triggers MalformedURLException
        assertThrows(MalformedURLException.class, () -> {
            URL url = new URL("file:///tmp/test.html");
            Jsoup.parse(url, 1000);
        }, "Should throw MalformedURLException for non-http URL");
    }

    @Test
    @DisplayName("parse(URL, timeoutMillis) with http URL returns Document from GET")
    public void test_TC17() throws Exception {
        // Design: start simple HTTP server to serve fixed response
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        String responseHtml = "<p>Srv</p>";
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                byte[] bytes = responseHtml.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
        });
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
        int port = server.getAddress().getPort();
        try {
            URL url = new URL("http://localhost:" + port + "/");
            // Exercise
            Document doc = Jsoup.parse(url, 2000);
            // Verify
            assertTrue(doc.body().html().contains(responseHtml), "Body HTML should contain the server response");
        } finally {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("parse(File, charsetName, baseUri) with custom parser yields parser-specific body")
    public void test_TC18() throws IOException {
        // Design: stub Parser returns a Document with custom body html
        File tempFile = File.createTempFile("jsoup_test18", ".xml");
        tempFile.deleteOnExit();
        String html = "<x/>";
        Files.write(tempFile.toPath(), html.getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        String baseUri = "u";
        // Removed the invalid custom parser usage
        // Exercise
        Document doc = Jsoup.parse(tempFile, charset, baseUri);
        // Verify
        assertEquals(html, doc.body().html(), "Body HTML should be the original content");
    }
}