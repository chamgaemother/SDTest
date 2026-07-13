package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.helper.HttpConnection;
import org.jsoup.Connection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("TC01: parse(html non-empty, baseUri non-empty) returns Document via Parser.parse(html, baseUri)")
    public void test_TC01() {
        String html = "<p>Test</p>";
        String baseUri = "http://example.com/";
        Document doc = Jsoup.parse(html, baseUri);
        assertEquals(baseUri, doc.baseUri(), "baseUri should be preserved");
        assertTrue(doc.html().contains("<p>Test</p>"), "HTML fragment should appear in output");
    }

    @Test
    @DisplayName("TC02: parse(html empty, baseUri empty) returns empty Document with no elements")
    public void test_TC02() {
        String html = "";
        String baseUri = "";
        Document doc = Jsoup.parse(html, baseUri);
        assertTrue(doc.body().children().isEmpty(), "Body should have no children for empty html");
        assertEquals("", doc.baseUri(), "BaseUri should be empty string");
    }

    @Test
    @DisplayName("TC03: parse(html, baseUri, parser) invokes parser.parseInput and returns its Document")
    public void test_TC03() {
        String html = "<div/>";
        String baseUri = "";
        Parser stubParser = new Parser(new Parser.Settings()) {
            @Override
            public Document parseInput(String inputHtml, String inputBaseUri) {
                Document d = Document.createShell(inputBaseUri);
                d.body().appendElement("marker").attr("id", "stub-marker");
                return d;
            }
        };
        Document doc = Jsoup.parse(html, baseUri, stubParser);
        assertNotNull(doc.getElementById("stub-marker"), "Should contain stub-marker from custom parser");
    }

    @Test
    @DisplayName("TC04: parse(html, parser) with empty html iterates parseInput with default baseUri")
    public void test_TC04() {
        String html = "";
        Parser parser = Parser.htmlParser();
        Document doc = Jsoup.parse(html, parser);
        assertEquals("", doc.baseUri(), "Default baseUri should be empty string on parse(html, parser)");
    }

    @Test
    @DisplayName("TC05: parse(html) uses Parser.parse(html, \"") default baseUri")
    public void test_TC05() {
        String html = "<span/>";
        Document doc = Jsoup.parse(html);
        assertEquals("", doc.baseUri(), "Default baseUri should be empty string");
        assertEquals(1, doc.select("span").size(), "Should parse one span element");
    }

    @Test
    @DisplayName("TC06: parse(File, charsetName, baseUri) throws IOException when file does not exist")
    public void test_TC06() {
        File file = new File("/path/does/not/exist_12345.html");
        String charset = "UTF-8";
        String baseUri = "http://example.com/";
        assertThrows(IOException.class, () -> Jsoup.parse(file, charset, baseUri),
                "Should throw IOException for missing file");
    }

    @Test
    @DisplayName("TC07: parse(File, null, baseUri) falls back to UTF-8 when meta not present")
    public void test_TC07() throws Exception {
        File tmp = File.createTempFile("test", ".html");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), "<p>abc</p>".getBytes(StandardCharsets.UTF_8));
        String charset = null;
        String baseUri = tmp.getAbsolutePath();
        Document doc = Jsoup.parse(tmp, charset, baseUri);
        assertEquals("abc", doc.select("p").text(), "Should read <p>abc</p> in UTF-8 fallback");
        assertEquals(baseUri, doc.baseUri(), "BaseUri should equal the file absolute path");
    }

    @Test
    @DisplayName("TC08: parse(URL, timeoutMillis) throws MalformedURLException for non-http URL")
    public void test_TC08() throws Exception {
        URL url = new URL("ftp://example.com/");
        int timeout = 1000;
        assertThrows(MalformedURLException.class, () -> Jsoup.parse(url, timeout),
                "Should throw MalformedURLException for non-http(s) URL");
    }

    @Test
    @DisplayName("TC09: parse(URL, timeoutMillis) returns Document after successful GET within timeout")
    public void test_TC09() throws Exception {
        URL url = new URL("http://example.com/");
        int timeout = 5000;
        Method connectMethod = HttpConnection.class.getDeclaredMethod("connect", URL.class);
        connectMethod.setAccessible(true);
        Connection stubConn = new Connection() {
            private int t;
            @Override public Connection timeout(int millis) { t = millis; return this; }
            @Override public Document customGet() throws IOException { // renamed method
                Document d = Document.createShell("");
                d.title("OK");
                return d;
            }
            @Override public Connection url(String u) {return this;}
            @Override public Connection userAgent(String userAgent) {return this;}
            @Override public Connection data(String key, String... value) {return this;}
            @Override public Connection cookie(String name, String value) {return this;}
            @Override public Connection requestBody(String body) {return this;}
            @Override public Connection header(String name, String value) {return this;}
            @Override public Connection ignoreHttpErrors(boolean ignore) {return this;}
            @Override public Connection ignoreContentType(boolean ignore) {return this;}
            @Override public Connection method(Connection.Method method) {return this;}
            @Override public Connection maxBodySize(int bytes) {return this;}
            @Override public Connection postDataCharset(String charset) {return this;}
            @Override public Connection referrer(String referrer) {return this;}
            @Override public Connection followRedirects(boolean followRedirects) {return this;}
            @Override public Connection parser(Parser parser) {return this;}
            @Override public Connection sslSocketFactory(javax.net.ssl.SSLSocketFactory factory) {return this;}
            @Override public Connection validator(Connection.Response response) {return this;}
            @Override public Connection request(org.jsoup.Connection.Request request) {return this;}
            @Override public org.jsoup.Connection.Request request() {return null;}
            @Override public Connection.Response response() { return new Connection.Response() {}; }
            @Override public Connection post() throws IOException { return this; }
        };
        Document doc = Jsoup.parse(url, timeout);
        assertEquals("OK", doc.title(), "Title should be 'OK' from stubbed GET");
    }

    @Test
    @DisplayName("TC10: parse(InputStream, charsetName, baseUri) throws IOException on invalid charsetName")
    public void test_TC10() {
        InputStream in = new ByteArrayInputStream("<p>X</p>".getBytes());
        String charset = "INVALID-CHARSET";
        String baseUri = "";
        assertThrows(IOException.class, () -> Jsoup.parse(in, charset, baseUri),
                "Should throw IOException for invalid charsetName");
    }
}