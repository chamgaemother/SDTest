package org.jsoup;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("TC01_O1: parse(html, baseUri) with non-empty html and baseUri returns Document with correct baseUri")
    public void test_TC01_O1() {
        String html = "<p>hello</p>";
        String baseUri = "http://example.com/";
        Document doc = Jsoup.parse(html, baseUri);
        assertAll(
            () -> assertEquals(baseUri, doc.baseUri(), "baseUri should be set to provided value"),
            () -> assertEquals("<p>hello</p>", doc.body().html(), "body HTML should reflect parsed content")
        );
    }

    @Test
    @DisplayName("TC02_O1: parse(html, \"") with empty baseUri returns Document with empty baseUri")
    public void test_TC02_O1() {
        String html = "<div/>";
        String baseUri = "";
        Document doc = Jsoup.parse(html, baseUri);
        assertAll(
            () -> assertEquals("", doc.baseUri(), "baseUri should be empty string"),
            () -> assertEquals("<div></div>", doc.body().html(), "self-closing tags normalized to opening and closing")
        );
    }

    @Test
    @DisplayName("TC03_O2: parse(html) delegates to parse(html, \"")")
    public void test_TC03_O2() {
        String html = "<span>t</span>";
        Document doc = Jsoup.parse(html);
        assertAll(
            () -> assertEquals("", doc.baseUri(), "delegated baseUri should be empty string"),
            () -> assertEquals("<span>t</span>", doc.body().html(), "body HTML should reflect parsed content")
        );
    }

    @Test
    @DisplayName("TC04_O3: parse(html, parser) with valid parser invokes parser.parseInput")
    public void test_TC04_O3() {
        String html = "x";
        // StubParser overrides parseInput to inject marker
        Parser stub = new Parser() {
            @Override
            public Document parseInput(String sourceHtml, String baseUri) {
                Document d = new Document(baseUri);
                d.title("stub");
                return d;
            }
        };
        Document doc = Jsoup.parse(html, stub);
        assertEquals("stub", doc.title(), "should reflect title set by stub parser");
    }

    @Test
    @DisplayName("TC05_O4: parse(html, baseUri, parser) with custom parser")
    public void test_TC05_O4() {
        String html = "x";
        String baseUri = "u";
        Parser stub = new Parser() {
            @Override
            public Document parseInput(String sourceHtml, String bu) {
                Document d = new Document(bu);
                d.title("p3");
                return d;
            }
        };
        Document doc = Jsoup.parse(html, baseUri, stub);
        assertAll(
            () -> assertEquals("u", doc.baseUri(), "baseUri should be as provided to parser"),
            () -> assertEquals("p3", doc.title(), "should reflect title set by stub parser")
        );
    }

    @Test
    @DisplayName("TC06_O5: parse(File, charset, baseUri) with valid file reads contents")
    public void test_TC06_O5() throws Exception {
        Path temp = Files.createTempFile("jsoup-test", ".html");
        Files.write(temp, "<h1>h</h1>".getBytes(StandardCharsets.UTF_8));
        File file = temp.toFile();
        String charset = "UTF-8";
        String baseUri = "u";
        Document doc = Jsoup.parse(file, charset, baseUri);
        assertAll(
            () -> assertEquals(baseUri, doc.baseUri(), "baseUri should be as provided"),
            () -> assertEquals("<h1>h</h1>", doc.body().html(), "body HTML should reflect file content")
        );
    }

    @Test
    @DisplayName("TC07_O5: parse(File, charset) delegates baseUri to file path")
    public void test_TC07_O5() throws Exception {
        Path temp = Files.createTempFile("jsoup-test", ".html");
        Files.write(temp, "<i>i</i>".getBytes(StandardCharsets.UTF_8));
        File file = temp.toFile();
        String charset = null;
        Document doc = Jsoup.parse(file, charset);
        assertEquals(file.getAbsolutePath(), doc.baseUri(), "baseUri should be file's absolute path when charset-only overload used");
    }

    @Test
    @DisplayName("TC08_O5: parse(File) with null charsetName delegates to load(file,null)")
    public void test_TC08_O5() throws Exception {
        Path temp = Files.createTempFile("jsoup-test", ".html");
        Files.write(temp, "<b/>\n".getBytes(StandardCharsets.UTF_8));
        File file = temp.toFile();
        Document doc = Jsoup.parse(file);
        assertTrue(doc.body().html().contains("<b>"), "body HTML should contain <b> after parsing");
    }

    @Test
    @DisplayName("TC09_O9: parse(URL, timeout) with non-http URL throws MalformedURLException")
    public void test_TC09_O9() throws Exception {
        URL url = new URL("ftp://x");
        int timeout = 1000;
        assertThrows(MalformedURLException.class,
            () -> Jsoup.parse(url, timeout),
            "Invalid protocol URL should throw MalformedURLException"
        );
    }

    @Test
    @DisplayName("TC10_O9: parse(URL, timeout) with valid http URL and timeout returns Document")
    public void test_TC10_O9() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                byte[] body = "<h2>ok</h2>".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, body.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(body);
                }
            }
        });
        server.start();
        try {
            URL url = new URL("http://127.0.0.1:" + server.getAddress().getPort() + "/");
            int timeout = 2000;
            Document doc = Jsoup.parse(url, timeout);
            assertEquals("<h2>ok</h2>", doc.body().html(), "body HTML should match server response");
        } finally {
            server.stop(0);
        }
    }
}