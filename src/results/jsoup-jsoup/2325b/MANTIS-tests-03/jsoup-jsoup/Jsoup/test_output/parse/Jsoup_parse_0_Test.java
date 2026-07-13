package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.OutputStream;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("parse(String, String) with non-empty html and baseUri delegates to Parser.parse")
    public void test_TC01_O1() {
        String html = "<p>Test</p>";
        String baseUri = "http://example.com/";
        Document doc = Jsoup.parse(html, baseUri);
        assertEquals("<p>Test</p>", doc.body().html());
    }

    @Test
    @DisplayName("parse(String, String, Parser) with custom parser parses via parser.parseInput")
    public void test_TC02_O2() {
        String html = "<root><a/></root>";
        String baseUri = "";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, baseUri, parser);
        assertEquals("text/xml", doc.contentType());
    }

    @Test
    @DisplayName("parse(String, Parser) with empty html delegates to parser.parseInput and uses \"\" baseUri")
    public void test_TC03_O3() {
        String html = "";
        Parser parser = Parser.htmlParser();
        Document doc = Jsoup.parse(html, parser);
        assertTrue(doc.body().html().isEmpty());
    }

    @Test
    @DisplayName("parse(String) uses Parser.parse(html, \"") to parse simple body")
    public void test_TC04_O4() {
        String html = "<div>Hi</div>";
        Document doc = Jsoup.parse(html);
        assertEquals("<div>Hi</div>", doc.body().html());
    }

    @Test
    @DisplayName("parse(File, String, String) with existing file returns parsed Document")
    public void test_TC05_O5() throws IOException {
        Path tmp = Files.createTempFile("test", ".html");
        Files.write(tmp, "<h1>Hdr</h1>".getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);
        File file = tmp.toFile();
        String charset = "UTF-8";
        String baseUri = "http://x/";
        Document doc = Jsoup.parse(file, charset, baseUri);
        assertEquals("<h1>Hdr</h1>", doc.body().html());
        tmp.toFile().delete();
    }

    @Test
    @DisplayName("parse(File, String) with null charsetName falls back to BOM/meta or UTF-8")
    public void test_TC06_O6() throws IOException {
        Path tmp = Files.createTempFile("utf", ".html");
        byte[] bom = {(byte)0xEF,(byte)0xBB,(byte)0xBF};
        byte[] content = "<p>UTF</p>".getBytes(StandardCharsets.UTF_8);
        Files.write(tmp, concat(bom, content));
        File file = tmp.toFile();
        Document doc = Jsoup.parse(file, null);
        assertEquals("<p>UTF</p>", doc.body().html());
        tmp.toFile().delete();
    }

    @Test
    @DisplayName("parse(File) with missing file throws IOException")
    public void test_TC07_O7() {
        File file = new File("does-not-exist.html");
        assertThrows(IOException.class, () -> Jsoup.parse(file));
    }

    @Test
    @DisplayName("parse(URL, int) with valid HTTP URL returns Document")
    public void test_TC08_O8() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                byte[] resp = "<p>OK</p>".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, resp.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(resp);
                }
            }
        });
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
        try {
            int port = server.getAddress().getPort();
            URL url = new URL("http://localhost:" + port + "/");
            Document doc = Jsoup.parse(url, 1000);
            assertEquals("<p>OK</p>", doc.body().html());
        } finally {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("parse(URL, int) with non-http URL throws MalformedURLException")
    public void test_TC09_O8() throws Exception {
        URL url = new URL("ftp://example.com/");
        assertThrows(MalformedURLException.class, () -> Jsoup.parse(url, 1000));
    }

    @Test
    @DisplayName("parse(URL, int) that times out throws SocketTimeoutException")
    public void test_TC10_O8() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/slow", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) { }
                byte[] resp = "<p>Slow</p>".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, resp.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(resp);
                }
            }
        });
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
        try {
            int port = server.getAddress().getPort();
            URL url = new URL("http://localhost:" + port + "/slow");
            assertThrows(SocketTimeoutException.class, () -> Jsoup.parse(url, 10));
        } finally {
            server.stop(0);
        }
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] r = new byte[a.length + b.length];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }
}