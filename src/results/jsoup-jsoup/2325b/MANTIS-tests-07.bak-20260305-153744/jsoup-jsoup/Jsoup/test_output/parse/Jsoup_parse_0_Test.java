package org.jsoup;

import org.jsoup.helper.DataUtil;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("parse(String, String) invokes Parser.parse with non-empty HTML and baseUri")
    public void test_TC01() {
        String html = "<p>Hello</p>";
        String baseUri = "http://example.com/";
        Document doc = Jsoup.parse(html, baseUri);
        assertAll(
            () -> assertEquals("<p>Hello</p>", doc.body().html()),
            () -> assertEquals(baseUri, doc.baseUri())
        );
    }

    @Test
    @DisplayName("parse(String, String, Parser) uses provided parser to parse input")
    public void test_TC02() {
        String html = "<div>X</div>";
        String baseUri = "";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, baseUri, parser);
        assertAll(
            () -> assertEquals(1, doc.select("div").size()),
            () -> assertTrue(doc.body().html().contains("<div"))
        );
    }

    @Test
    @DisplayName("parse(String, Parser) uses empty baseUri when none provided")
    public void test_TC03() {
        String html = "<span>Y</span>";
        Parser parser = Parser.htmlParser();
        Document doc = Jsoup.parse(html, parser);
        assertAll(
            () -> assertEquals("", doc.baseUri()),
            () -> assertTrue(doc.body().html().contains("<span>Y"))
        );
    }

    @Test
    @DisplayName("parse(String) defaults to empty baseUri and HTML parser")
    public void test_TC04() {
        String html = "<b>B</b>";
        Document doc = Jsoup.parse(html);
        assertAll(
            () -> assertEquals("<b>B</b>", doc.body().html()),
            () -> assertEquals("", doc.baseUri())
        );
    }

    @Test
    @DisplayName("parse(File, String, String) loads file with explicit charsetName and baseUri")
    public void test_TC05() throws IOException {
        Path temp = Files.createTempFile("test", ".html");
        Files.write(temp, "<i>F</i>".getBytes("UTF-8"));
        File f = temp.toFile();
        String charset = "UTF-8";
        String baseUri = "http://base/";
        Document doc = Jsoup.parse(f, charset, baseUri);
        assertAll(
            () -> assertEquals("<i>F</i>", doc.body().html()),
            () -> assertEquals(baseUri, doc.baseUri())
        );
    }

    @Test
    @DisplayName("parse(File, String) uses file path as baseUri when baseUri omitted")
    public void test_TC06() throws IOException {
        Path temp = Files.createTempFile("test2", ".html");
        Files.write(temp, "<u>U</u>".getBytes("UTF-8"));
        File f = temp.toFile();
        String charset = "UTF-8";
        Document doc = Jsoup.parse(f, charset);
        assertAll(
            () -> assertEquals("<u>U</u>", doc.body().html()),
            () -> assertEquals(f.getAbsolutePath(), doc.baseUri())
        );
    }

    @Test
    @DisplayName("parse(File) detects charset BOM or meta and uses file path as baseUri")
    public void test_TC07() throws IOException {
        byte[] bom = new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF};
        Path temp = Files.createTempFile("test3", ".html");
        try (FileOutputStream out = new FileOutputStream(temp.toFile())) {
            out.write(bom);
            out.write("<em>E</em>".getBytes("UTF-8"));
        }
        Document doc = Jsoup.parse(temp.toFile());
        assertAll(
            () -> assertEquals("<em>E</em>", doc.body().html()),
            () -> assertEquals(temp.toFile().getAbsolutePath(), doc.baseUri())
        );
    }

    @Test
    @DisplayName("parse(File, String, String, Parser) uses custom parser for files")
    public void test_TC08() throws IOException {
        Path temp = Files.createTempFile("test4", ".xml");
        Files.write(temp, "<item>1</item>".getBytes("UTF-8"));
        File f = temp.toFile();
        Parser stub = Parser.htmlParser(); // Changed to use a valid parser
        Document doc = Jsoup.parse(f, "UTF-8", "u", stub);
        assertTrue(doc.html().contains("marker-from-stub"));
    }

    @Test
    @DisplayName("parse(Path, String, String) loads from Path with explicit charset and baseUri")
    public void test_TC09() throws IOException {
        Path p = Files.createTempFile("test5", ".html");
        Files.write(p, "<h1>H</h1>".getBytes("UTF-8"));
        Document doc = Jsoup.parse(p, "UTF-8", "http://x");
        assertAll(
            () -> assertEquals("<h1>H</h1>", doc.body().html()),
            () -> assertEquals("http://x", doc.baseUri())
        );
    }

    @Test
    @DisplayName("parse(Path, String) uses path.toAbsolutePath as baseUri")
    public void test_TC10() throws IOException {
        Path p = Files.createTempFile("test6", ".html");
        Files.write(p, "<h2>H2</h2>".getBytes("UTF-8"));
        Document doc = Jsoup.parse(p, "UTF-8");
        assertEquals(p.toAbsolutePath().toString(), doc.baseUri());
    }

    @Test
    @DisplayName("parse(Path) auto-detect charset uses path.toAbsolutePath")
    public void test_TC11() throws IOException {
        byte[] bom = new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF};
        Path p = Files.createTempFile("test7", ".html");
        try (FileOutputStream out = new FileOutputStream(p.toFile())) {
            out.write(bom);
            out.write("<h3>H3</h3>".getBytes("UTF-8"));
        }
        Document doc = Jsoup.parse(p);
        assertEquals("<h3>H3</h3>", doc.body().html());
    }

    @Test
    @DisplayName("parse(Path, String, String, Parser) uses custom parser for Path overload")
    public void test_TC12() throws IOException {
        Path p = Files.createTempFile("test8", ".xml");
        Files.write(p, "<e>8</e>".getBytes("UTF-8"));
        Parser stub = Parser.htmlParser(); // Changed to use a valid parser
        Document doc = Jsoup.parse(p, null, "u", stub);
        assertTrue(doc.html().contains("stubbed"));
    }

    @Test
    @DisplayName("parse(InputStream, String, String) reads from stream and sets baseUri")
    public void test_TC13() throws IOException {
        InputStream in = new ByteArrayInputStream("<p>S</p>".getBytes());
        Document doc = Jsoup.parse(in, null, "u");
        assertAll(
            () -> assertEquals("<p>S</p>", doc.body().html()),
            () -> assertEquals("u", doc.baseUri())
        );
    }

    @Test
    @DisplayName("parse(InputStream, String, String, Parser) uses custom parser on InputStream")
    public void test_TC14() throws IOException {
        InputStream in = new ByteArrayInputStream("<x>1</x>".getBytes());
        Parser stub = Parser.htmlParser(); // Changed to use a valid parser
        Document doc = Jsoup.parse(in, null, "", stub);
        assertTrue(doc.html().contains("stubbed"));
    }

    @Test
    @DisplayName("parse(URL, int) throws MalformedURLException for unsupported protocol")
    public void test_TC15() {
        assertThrows(MalformedURLException.class, () -> {
            URL url = new URL("ftp://example.com");
            Jsoup.parse(url, 1000);
        });
    }

    @Test
    @DisplayName("parse(URL, int) throws SocketTimeoutException when timeout exceeded")
    public void test_TC16() throws Exception {
        URL url = new URL("http://10.255.255.1");
        assertThrows(SocketTimeoutException.class, () -> Jsoup.parse(url, 1));
    }

    @Test
    @DisplayName("parse(URL, int) returns Document for valid HTTP URL within timeout")
    public void test_TC17() throws Exception {
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/test", exchange -> {
            byte[] resp = "<p>Z</p>".getBytes();
            exchange.sendResponseHeaders(200, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.close();
        });
        server.start();
        try {
            int port = server.getAddress().getPort();
            URL url = new URL("http://localhost:" + port + "/test");
            Document doc = Jsoup.parse(url, 2000);
            assertEquals("<p>Z</p>", doc.body().html());
        } finally {
            server.stop(0);
        }
    }
}