package org.jsoup;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
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
public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("TC11: parse(File, null charset, baseUri) loads HTML with BOM/meta-detected charset")
    public void test_TC11() throws Exception {
        // create temp file with UTF-8 content
        File tmp = File.createTempFile("tc11", ".html");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), "<p>File</p>".getBytes("UTF-8"));
        String baseUri = "http://host/";
        // path covers B0→B1→B3→B5 where charsetName null triggers charset detection
        Document doc = Jsoup.parse(tmp, null, baseUri);
        assertEquals("<p>File</p>", doc.body().html());
        assertEquals(baseUri, doc.baseUri());
    }

    @Test
    @DisplayName("TC12: parse(File, 'UTF-8' charset) uses file path as baseUri")
    public void test_TC12() throws Exception {
        File tmp = File.createTempFile("tc12", ".html");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), "<div>Text</div>".getBytes("UTF-8"));
        // explicit charset forces branch B4→B5
        Document doc = Jsoup.parse(tmp, "UTF-8");
        assertEquals("<div>Text</div>", doc.body().html());
        assertEquals(tmp.getAbsolutePath(), doc.baseUri());
    }

    @Test
    @DisplayName("TC13: parse(File) loads file with default null charset and path baseUri")
    public void test_TC13() throws Exception {
        File tmp = File.createTempFile("tc13", ".html");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), "<span>One</span>".getBytes("UTF-8"));
        // default overload calls parse(file, null, path)
        Document doc = Jsoup.parse(tmp);
        assertEquals("<span>One</span>", doc.body().html());
        assertEquals(tmp.getAbsolutePath(), doc.baseUri());
    }

    @Test
    @DisplayName("TC14: parse(File, charset, baseUri, parser) uses custom XML parser")
    public void test_TC14() throws Exception {
        File tmp = File.createTempFile("tc14", ".xml");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), "<root/>".getBytes("UTF-8"));
        String base = "base";
        Parser p = Parser.xmlParser();
        // path B0→B2→B6→B7 uses custom parser branch
        Document doc = Jsoup.parse(tmp, "UTF-8", base, p);
        assertEquals("<root/>", doc.body().html());
        assertEquals(base, doc.baseUri());
    }

    @Test
    @DisplayName("TC15: parse(Path, null charset, baseUri) loads HTML from Path")
    public void test_TC15() throws Exception {
        Path path = Files.createTempFile("tc15", ".html");
        path.toFile().deleteOnExit();
        Files.write(path, "<p>Path</p>".getBytes("UTF-8"));
        String base = "http://x/";
        Document doc = Jsoup.parse(path, null, base);
        assertEquals("<p>Path</p>", doc.body().html());
        assertEquals(base, doc.baseUri());
    }

    @Test
    @DisplayName("TC16: parse(Path, 'UTF-8') uses path as baseUri")
    public void test_TC16() throws Exception {
        Path path = Files.createTempFile("tc16", ".html");
        path.toFile().deleteOnExit();
        Files.write(path, "<b>Bold</b>".getBytes("UTF-8"));
        Document doc = Jsoup.parse(path, "UTF-8");
        assertEquals("<b>Bold</b>", doc.body().html());
        assertEquals(path.toAbsolutePath().toString(), doc.baseUri());
    }

    @Test
    @DisplayName("TC17: parse(Path) reads Path with default null charset")
    public void test_TC17() throws Exception {
        Path path = Files.createTempFile("tc17", ".html");
        path.toFile().deleteOnExit();
        Files.write(path, "<i>Italics</i>".getBytes("UTF-8"));
        Document doc = Jsoup.parse(path);
        assertEquals("<i>Italics</i>", doc.body().html());
        assertEquals(path.toAbsolutePath().toString(), doc.baseUri());
    }

    @Test
    @DisplayName("TC18: parse(Path, charset, baseUri, parser) custom XML parser on Path")
    public void test_TC18() throws Exception {
        Path path = Files.createTempFile("tc18", ".xml");
        path.toFile().deleteOnExit();
        Files.write(path, "<x/>".getBytes("UTF-8"));
        String base = "uri";
        Parser pr = Parser.xmlParser();
        Document doc = Jsoup.parse(path, "UTF-8", base, pr);
        assertEquals("<x/>", doc.body().html());
        assertEquals(base, doc.baseUri());
    }

    static class TrackCloseStream extends ByteArrayInputStream {
        boolean closed = false;
        TrackCloseStream(byte[] buf) { super(buf); }
        @Override public void close() throws IOException { super.close(); closed = true; }
    }

    @Test
    @DisplayName("TC19: parse(InputStream, charset, baseUri) reads and closes stream")
    public void test_TC19() throws Exception {
        TrackCloseStream in = new TrackCloseStream("<h1>Head</h1>".getBytes("UTF-8"));
        String base = "b/";
        Document doc = Jsoup.parse(in, "UTF-8", base);
        assertEquals("<h1>Head</h1>", doc.body().html());
        assertTrue(in.closed, "InputStream should be closed after parsing");
    }

    @Test
    @DisplayName("TC20: parse(InputStream, charset, baseUri, parser) uses custom parser")
    public void test_TC20() throws Exception {
        TrackCloseStream in = new TrackCloseStream("<u/>".getBytes("UTF-8"));
        Parser pr = Parser.xmlParser();
        // passing null charset routes to correct overload with parser branch
        Document doc = Jsoup.parse(in, null, "", pr);
        assertEquals("<u/>", doc.body().html());
    }

    @Test
    @DisplayName("TC21: parse(URL, timeout) gets remote HTML successfully")
    public void test_TC21() throws Exception {
        // start local HTTP server that serves <p>Hello</p>
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                byte[] resp = "<p>Hello</p>".getBytes("UTF-8");
                exchange.sendResponseHeaders(200, resp.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(resp);
                }
            }
        });
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
        try {
            URL url = new URL("http://localhost:" + server.getAddress().getPort() + "/");
            Document doc = Jsoup.parse(url, 1000);
            assertEquals("<p>Hello</p>", doc.body().html());
        } finally {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("TC22: parse(URL, timeout) throws MalformedURLException for unsupported protocol")
    public void test_TC22() throws Exception {
        URL url = new URL("ftp://example.com/");
        assertThrows(MalformedURLException.class, () -> Jsoup.parse(url, 500));
    }

    @Test
    @DisplayName("TC23: clean(empty baseUri, safelist.preserveRelativeLinks=true) sets DummyUri branch")
    public void test_TC23() {
        Safelist sl = Safelist.relaxed().preserveRelativeLinks(true);
        String out = Jsoup.clean("<a href='/x'>x</a>", "", sl);
        assertTrue(out.contains("href=\"/x\""));
    }

    @Test
    @DisplayName("TC24: clean(non-empty baseUri, default safelist) skips DummyUri branch")
    public void test_TC24() {
        Safelist sl = Safelist.simpleText();
        String out = Jsoup.clean("<p><b>bold</b></p>", "http://u/", sl);
        assertEquals("bold", out);
    }

    @Test
    @DisplayName("TC25: clean(html, baseUri, safelist, outputSettings) applies settings")
    public void test_TC25() {
        Document.OutputSettings os = new Document.OutputSettings().prettyPrint(false);
        Safelist sl = Safelist.basic();
        String out = Jsoup.clean("<div>1</div><div>2</div>", "", sl, os);
        assertEquals("<div>1</div><div>2</div>", out);
    }

    @Test
    @DisplayName("TC26: isValid returns true for HTML matching safelist")
    public void test_TC26() {
        Safelist sl = Safelist.relaxed();
        boolean v = Jsoup.isValid("<p>ok</p>", sl);
        assertTrue(v);
    }

    @Test
    @DisplayName("TC27: isValid returns false for disallowed tags")
    public void test_TC27() {
        Safelist sl = Safelist.basic();
        boolean v = Jsoup.isValid("<script>alert(1)</script>", sl);
        assertFalse(v);
    }
}